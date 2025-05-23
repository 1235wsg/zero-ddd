package com.zjj.graphql.component.query.fuzzy;

import com.querydsl.core.types.EntityPath;
import graphql.schema.DataFetcher;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.util.TypeInformation;
import org.springframework.graphql.data.pagination.CursorEncoder;
import org.springframework.graphql.data.pagination.CursorStrategy;
import org.springframework.graphql.data.query.QuerydslDataFetcher;
import org.springframework.graphql.data.query.ScrollPositionCursorStrategy;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * @author zengJiaJun
 * @version 1.0
 * @crateTime 2024年10月19日 23:30
 */
public class ReactiveFuzzyBuilder<T, R> {

    private static final QuerydslBinderCustomizer NO_OP_BINDER_CUSTOMIZER = (bindings, root) -> {
    };

    private final ReactiveQuerydslPredicateExecutor<T> executor;

    private final TypeInformation<T> domainType;

    private final Class<R> resultType;

    @Nullable
    private final CursorStrategy<ScrollPosition> cursorStrategy;

    @Nullable
    private final Integer defaultScrollCount;

    @Nullable
    private final Function<Boolean, ScrollPosition> defaultScrollPosition;

    private final Sort sort;

    private final QuerydslBinderCustomizer<? extends EntityPath<T>> customizer;

    @SuppressWarnings("unchecked")
    public ReactiveFuzzyBuilder(ReactiveQuerydslPredicateExecutor<T> executor, Class<R> domainType) {
        this(executor, TypeInformation.of((Class<T>) domainType),
                domainType, null, null, null, Sort.unsorted(), NO_OP_BINDER_CUSTOMIZER);
    }

    public ReactiveFuzzyBuilder(
            ReactiveQuerydslPredicateExecutor<T> executor, TypeInformation<T> domainType, Class<R> resultType,
            @Nullable CursorStrategy<ScrollPosition> cursorStrategy,
            @Nullable Integer defaultScrollCount, @Nullable Function<Boolean, ScrollPosition> defaultScrollPosition,
            Sort sort, QuerydslBinderCustomizer<? extends EntityPath<T>> customizer) {

        this.executor = executor;
        this.domainType = domainType;
        this.resultType = resultType;
        this.cursorStrategy = cursorStrategy;
        this.defaultScrollCount = defaultScrollCount;
        this.defaultScrollPosition = defaultScrollPosition;
        this.sort = sort;
        this.customizer = customizer;
    }

    /**
     * Project results returned from the {@link ReactiveQuerydslPredicateExecutor}
     * into the target {@code projectionType}. Projection types can be
     * either interfaces with property getters to expose or regular classes
     * outside the entity type hierarchy for DTO projections.
     * @param <P> projection type
     * @param projectionType projection type
     * @return a new {@link QuerydslDataFetcher.Builder} instance with all previously
     * configured options and {@code projectionType} applied
     */
    public <P> ReactiveFuzzyBuilder<T, P> projectAs(Class<P> projectionType) {
        Assert.notNull(projectionType, "Projection type must not be null");
        return new ReactiveFuzzyBuilder<>(this.executor, this.domainType, projectionType,
                this.cursorStrategy, this.defaultScrollCount, this.defaultScrollPosition,
                this.sort, this.customizer);
    }

    /**
     * Configure strategy for decoding a cursor from a paginated request.
     * <p>By default, this is {@link ScrollPositionCursorStrategy} with
     * {@link CursorEncoder#base64()} encoding.
     * @param cursorStrategy the strategy to use
     * @return a new {@link QuerydslDataFetcher.Builder} instance with all previously configured
     * options and {@code Sort} applied
     * @since 1.2.0
     */
    public ReactiveFuzzyBuilder<T, R> cursorStrategy(@Nullable CursorStrategy<ScrollPosition> cursorStrategy) {
        return new ReactiveFuzzyBuilder<>(this.executor, this.domainType, this.resultType,
                cursorStrategy, this.defaultScrollCount, this.defaultScrollPosition,
                this.sort, this.customizer);
    }

    /**
     * Configure a default scroll count to use, and function to return a default
     * {@link ScrollPosition} for forward vs backward pagination.
     * <p>For offset scrolling, use {@link ScrollPosition#offset()} to scroll
     * from the beginning. Currently, it is not possible to go back from the end.
     * <p>For keyset scrolling, use {@link ScrollPosition#keyset()} to scroll
     * from the beginning, or {@link KeysetScrollPosition#reverse()} the same
     * to go back from the end.
     * <p>By default a count of 20 and {@link ScrollPosition#offset()} are used.
     * @param defaultCount the default element count in the subrange
     * @param defaultPosition function that returns the default scroll position
     * @since 1.2.5
     */
    public ReactiveFuzzyBuilder<T, R> defaultScrollSubrange(
            int defaultCount, Function<Boolean, ScrollPosition> defaultPosition) {

        return new ReactiveFuzzyBuilder<>(this.executor, this.domainType, this.resultType,
                this.cursorStrategy, defaultCount, defaultPosition, this.sort, this.customizer);
    }

    /**
     * Configure a {@link ScrollSubrange} to use when a paginated request does
     * not specify a cursor and/or a count of items.
     * <p>By default, this is {@link OffsetScrollPosition#offset()} with a count of 20.
     * @param defaultSubrange the default scroll subrange
     * @return a new {@link QuerydslDataFetcher.Builder} instance
     * @since 1.2.0
     * @deprecated in favor of {@link #defaultScrollSubrange(int, Function)}
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Deprecated(since = "1.2.5", forRemoval = true)
    public ReactiveFuzzyBuilder<T, R> defaultScrollSubrange(@Nullable ScrollSubrange defaultSubrange) {
        return new ReactiveFuzzyBuilder<>(this.executor, this.domainType, this.resultType,
                this.cursorStrategy,
                (defaultSubrange != null) ? defaultSubrange.count().getAsInt() : null,
                (defaultSubrange != null) ? (forward) -> defaultSubrange.position().get() : null,
                this.sort, this.customizer);
    }

    /**
     * Apply a {@link Sort} order.
     * @param sort the default sort order
     * @return a new {@link QuerydslDataFetcher.Builder} instance with all previously configured
     * options and {@code Sort} applied
     */
    public ReactiveFuzzyBuilder<T, R> sortBy(Sort sort) {
        Assert.notNull(sort, "Sort must not be null");
        return new ReactiveFuzzyBuilder<>(this.executor, this.domainType, this.resultType,
                this.cursorStrategy, this.defaultScrollCount, this.defaultScrollPosition,
                sort, this.customizer);
    }

    /**
     * Apply a {@link QuerydslBinderCustomizer}.
     *
     * <p>If a Querydsl repository implements {@link QuerydslBinderCustomizer}
     * itself, this is automatically detected and applied during
     * {@link QuerydslFuzzyDataFetcher#autoRegistrationConfigurer(List, List) auto-registration}.
     * For manual registration, you will need to use this method to apply it.
     * @param customizer to customize the GraphQL query to Querydsl
     * Predicate binding with
     * @return a new {@link QuerydslDataFetcher.Builder} instance with all previously configured
     * options and {@code QuerydslBinderCustomizer} applied
     */
    public ReactiveFuzzyBuilder<T, R> customizer(QuerydslBinderCustomizer<? extends EntityPath<T>> customizer) {
        Assert.notNull(customizer, "QuerydslBinderCustomizer must not be null");
        return new ReactiveFuzzyBuilder<>(this.executor, this.domainType, this.resultType,
                this.cursorStrategy, this.defaultScrollCount, this.defaultScrollPosition,
                this.sort, customizer);
    }

    /**
     * Build a {@link DataFetcher} to fetch single object instances}.
     */
    public DataFetcher<Mono<R>> single() {
        return null;
//        return new QuerydslDataFetcher.ReactiveSingleEntityFetcher<>(
//                this.executor, this.domainType, this.resultType, this.sort, this.customizer);
    }

    /**
     * Build a {@link DataFetcher} to fetch many object instances.
     */
    public DataFetcher<Flux<R>> many() {
        return null;
//        return new QuerydslDataFetcher.ReactiveManyEntityFetcher<>(
//                this.executor, this.domainType, this.resultType, this.sort, this.customizer);
    }

    /**
     * Build a {@link DataFetcher} that scrolls and returns
     * {@link org.springframework.data.domain.Window}.
     * @since 1.2.0
     */
    public DataFetcher<Mono<Iterable<R>>> scrollable() {
        return null;
//        return new QuerydslDataFetcher.ReactiveScrollableEntityFetcher<>(
//                this.executor, this.domainType, this.resultType,
//                (this.cursorStrategy != null) ? this.cursorStrategy : RepositoryUtils.defaultCursorStrategy(),
//                (this.defaultScrollCount != null) ? this.defaultScrollCount : RepositoryUtils.defaultScrollCount(),
//                (this.defaultScrollPosition != null) ? this.defaultScrollPosition : RepositoryUtils.defaultScrollPosition(),
//                this.sort, this.customizer);
    }
}
