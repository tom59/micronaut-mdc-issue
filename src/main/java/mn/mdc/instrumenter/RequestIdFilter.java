package mn.mdc.instrumenter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Filter("/**")
public class RequestIdFilter extends OncePerRequestHttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestIdFilter.class);

    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        String traceIdHeader = request.getHeaders().get("traceId");
        if (MDC.get(TRACE_ID_MDC_KEY) != null) {
            LOG.warn("MDC should have been empty here.");
        }
        LOG.info("Storing traceId in MDC: " + traceIdHeader);
        MDC.put(TRACE_ID_MDC_KEY, traceIdHeader);
        return Flowable.fromPublisher(chain.proceed(request))
                .doFinally(() -> {
                    LOG.info("Removing traceId id from MDC: {}", MDC.get(TRACE_ID_MDC_KEY));
                    MDC.remove(TRACE_ID_MDC_KEY);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
