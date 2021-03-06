package ru.skogmark.aggregator.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import ru.skogmark.aggregator.core.content.Content;
import ru.skogmark.aggregator.core.content.ContentPost;
import ru.skogmark.aggregator.core.content.ParsingContext;
import ru.skogmark.aggregator.core.content.SourceService;
import ru.skogmark.aggregator.core.moderation.PremoderationQueueService;
import ru.skogmark.aggregator.core.moderation.UnmoderatedPost;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class Worker implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    private final ScheduledExecutorService workerExecutor;
    private final ExecutorService taskExecutor;
    private final List<ChannelContext> channelContexts;
    private final SourceService sourceService;
    private final PremoderationQueueService premoderationQueueService;
    private final ParsingTimeStorage parsingTimeStorage;

    Worker(@Nonnull ScheduledExecutorService workerExecutor,
           @Nonnull ExecutorService taskExecutor,
           @Nonnull List<ChannelContext> channelContexts,
           @Nonnull SourceService sourceService,
           @Nonnull PremoderationQueueService premoderationQueueService,
           @Nonnull ParsingTimeStorage parsingTimeStorage) {
        this.workerExecutor = requireNonNull(workerExecutor, "workerExecutor");
        this.taskExecutor = requireNonNull(taskExecutor, "taskExecutor");
        this.channelContexts = requireNonNull(channelContexts, "channelContexts");
        this.sourceService = requireNonNull(sourceService, "sourceService");
        this.premoderationQueueService = requireNonNull(premoderationQueueService, "premoderationQueueService");
        this.parsingTimeStorage = requireNonNull(parsingTimeStorage, "parsingTimeStorage");
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Starting worker");
        workerExecutor.scheduleWithFixedDelay(() -> channelContexts
                        .forEach(channelContext -> channelContext.getSourceContexts()
                                .forEach(sourceContext -> parseContentIfNeeded(
                                        channelContext.getChannelId(), sourceContext, ZonedDateTime.now()))),
                0, 1, TimeUnit.SECONDS);
    }

    void parseContentIfNeeded(int channelId, SourceContext sourceContext, ZonedDateTime parsingTime) {
        if (parsingTimeStorage.minuteExists(channelId, sourceContext.getSourceId(), parsingTime)) {
            log.debug("SourceContext has been parsed already at this minute: sourceContext={}", sourceContext);
            return;
        }
        if (!isTimeMatched(parsingTime, sourceContext.getTimetable())) {
            log.debug("It's not the time to parse content for: sourceContext={}", sourceContext);
            return;
        }
        log.info("It's time to parse content for: channelId={}, sourceContext={}", channelId, sourceContext);
        parseContentAsync(sourceContext, channelId);
        parsingTimeStorage.put(channelId, sourceContext.getSourceId(), parsingTime);
    }

    static boolean isTimeMatched(ZonedDateTime startingTime, Timetable timetable) {
        return timetable.getTimes().stream()
                .anyMatch(time ->
                        startingTime.getHour() == time.getHour() && startingTime.getMinute() == time.getMinute());
    }

    public void parseContentAsync(SourceContext sourceContext, int channelId) {
        log.info("parseContentAsync(): sourceId={}, channelId={}", sourceContext.getSourceId(), channelId);
        taskExecutor.execute(() -> {
            Long offset = sourceService.getOffset(sourceContext.getSourceId()).orElse(null);
            Optional<Content> content = sourceContext.getParser().parse(ParsingContext.builder()
                    .setSourceId(sourceContext.getSourceId())
                    .setLimit(sourceContext.getParserLimit())
                    .setOffset(offset)
                    .build());
            if (content.isEmpty()) {
                log.warn("Content is empty: sourceId={}", sourceContext.getSourceId());
                return;
            }

            log.info("Content obtained: sourceId={}, content={}", sourceContext.getSourceId(), content);
            premoderationQueueService.enqueuePosts(content.get().getPosts().stream()
                    .map(post -> toUnmoderatedPost(post, channelId))
                    .collect(Collectors.toList()));
            if (!content.get().getNextOffset().equals(offset)) {
                sourceService.upsertOffset(sourceContext.getSourceId(), content.get().getNextOffset());
            }
        });
    }

    private static UnmoderatedPost toUnmoderatedPost(@Nonnull ContentPost contentPost, int channelId) {
        requireNonNull(contentPost, "contentPost");
        return UnmoderatedPost.builder()
                .setChannelId(channelId)
                .setTitle(contentPost.getTitle().orElse(null))
                .setText(contentPost.getText().orElse(null))
                .setImages(contentPost.getImages())
                .build();
    }
}
