package mn.mdc.instrumenter;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

import static io.micronaut.http.HttpRequest.GET;
import static mn.mdc.instrumenter.RequestIdFilter.TRACE_ID_MDC_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MDCTest {

    @Controller
    public static class MDCController {

        @Get("/mdc-test")
        HttpResponse<String> getMdc() {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            return HttpResponse.ok(mdc.get(TRACE_ID_MDC_KEY));
        }
    }

    @Test
    void test_mdc() {
        RxHttpClient client = startAppAndGetClient();

        for(int i=0; i< 100; i++) {
            String traceId = UUID.randomUUID().toString();
            HttpRequest<Object> request = GET("/mdc-test").header("traceId", traceId);
            String response = client.retrieve(request).blockingFirst();
            assertEquals(response, traceId);
        }
    }

    private RxHttpClient startAppAndGetClient() {
        ApplicationContext applicationContext = ApplicationContext.build().build();
        EmbeddedServer embeddedServer = applicationContext.start().getBean(EmbeddedServer.class).start();
        return RxHttpClient.create(embeddedServer.getURL());
    }
}
