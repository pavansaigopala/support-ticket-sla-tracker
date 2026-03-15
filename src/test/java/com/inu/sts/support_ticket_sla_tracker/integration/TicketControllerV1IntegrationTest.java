package com.inu.sts.support_ticket_sla_tracker.integration;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TicketControllerV1IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/tickets";
    }

    private TestRestTemplate asAgent() {
        return restTemplate.withBasicAuth("agent", "agent");
    }

    private TestRestTemplate asViewer() {
        return restTemplate.withBasicAuth("viewer", "viewer");
    }

    @Nested
    @DisplayName("Ticket create + fetch")
    class CreateAndFetch {

        @Test
        void createThenFetch_returnsSameTicket() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Integration test ticket")
                    .description("Created by integration test")
                    .priority(Priority.HIGH)
                    .customerId("cust-integration")
                    .build();

            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(
                    baseUrl(),
                    create,
                    TicketResponse.class);

            assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            TicketResponse created = createResp.getBody();
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(created.getTitle()).isEqualTo("Integration test ticket");
            assertThat(created.getStatus()).isEqualTo(TicketStatus.NEW);
            assertThat(created.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(created.getCustomerId()).isEqualTo("cust-integration");

            ResponseEntity<TicketResponse> getResp = asAgent().getForEntity(
                    baseUrl() + "/" + created.getId(),
                    TicketResponse.class);

            assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            TicketResponse fetched = getResp.getBody();
            assertThat(fetched).isNotNull();
            assertThat(fetched.getId()).isEqualTo(created.getId());
            assertThat(fetched.getTitle()).isEqualTo(created.getTitle());
            assertThat(fetched.getStatus()).isEqualTo(created.getStatus());
        }
    }

    @Nested
    @DisplayName("Status change: RESOLVED requires comment")
    class ResolvedRequiresComment {

        @Test
        void updateStatus_toResolvedWithoutComment_returns400() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Ticket for resolved rule")
                    .priority(Priority.MEDIUM)
                    .customerId("cust1")
                    .build();
            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(baseUrl(), create, TicketResponse.class);
            assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            UUID ticketId = createResp.getBody().getId();

            StatusUpdateRequest statusReq = StatusUpdateRequest.builder().status(TicketStatus.RESOLVED).build();
            ResponseEntity<ErrorResponse> statusResp = asAgent().exchange(
                    baseUrl() + "/" + ticketId + "/status",
                    HttpMethod.POST,
                    new HttpEntity<>(statusReq, jsonHeaders()),
                    ErrorResponse.class);

            assertThat(statusResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(statusResp.getBody()).isNotNull();
            assertThat(statusResp.getBody().getMessage()).contains("at least one comment");
        }

        @Test
        void addCommentThenResolve_succeeds() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Ticket to resolve with comment")
                    .priority(Priority.LOW)
                    .customerId("cust2")
                    .build();
            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(baseUrl(), create, TicketResponse.class);
            assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            UUID ticketId = createResp.getBody().getId();

            CommentCreateRequest commentReq = CommentCreateRequest.builder()
                    .comment("Resolution: fixed in prod")
                    .author("agent1")
                    .build();
            ResponseEntity<CommentResponse> commentResp = asAgent().postForEntity(
                    baseUrl() + "/" + ticketId + "/comments",
                    commentReq,
                    CommentResponse.class);
            assertThat(commentResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            StatusUpdateRequest statusReq = StatusUpdateRequest.builder().status(TicketStatus.RESOLVED).build();
            ResponseEntity<TicketResponse> statusResp = asAgent().exchange(
                    baseUrl() + "/" + ticketId + "/status",
                    HttpMethod.POST,
                    new HttpEntity<>(statusReq, jsonHeaders()),
                    TicketResponse.class);

            assertThat(statusResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(statusResp.getBody().getStatus()).isEqualTo(TicketStatus.RESOLVED);
        }
    }

    @Nested
    @DisplayName("Optimistic locking (409)")
    class OptimisticLocking {

        @Test
        void concurrentPatch_oneSucceedsOneReturns409() throws Exception {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Original title")
                    .priority(Priority.HIGH)
                    .customerId("cust1")
                    .build();
            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(baseUrl(), create, TicketResponse.class);
            assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            UUID ticketId = createResp.getBody().getId();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch start = new CountDownLatch(1);

            Callable<ResponseEntity<String>> patch1 = () -> {
                start.await();
                return asAgent().exchange(
                        baseUrl() + "/" + ticketId,
                        HttpMethod.PATCH,
                        new HttpEntity<>(TicketUpdateRequest.builder().title("First update").build(), jsonHeaders()),
                        String.class);
            };
            Callable<ResponseEntity<String>> patch2 = () -> {
                start.await();
                return asAgent().exchange(
                        baseUrl() + "/" + ticketId,
                        HttpMethod.PATCH,
                        new HttpEntity<>(TicketUpdateRequest.builder().title("Second update").build(), jsonHeaders()),
                        String.class);
            };

            Future<ResponseEntity<String>> f1 = executor.submit(patch1);
            Future<ResponseEntity<String>> f2 = executor.submit(patch2);
            start.countDown();

            ResponseEntity<String> r1 = f1.get(10, TimeUnit.SECONDS);
            ResponseEntity<String> r2 = f2.get(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(r1.getStatusCode().is2xxSuccessful() || r2.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(r1.getStatusCode().value() == 409 || r2.getStatusCode().value() == 409).isTrue();

            ResponseEntity<String> conflictResp = r1.getStatusCode().value() == 409 ? r1 : r2;
            assertThat(conflictResp.getBody()).contains("updated by another request");
        }
    }

    @Nested
    @DisplayName("Security: VIEWER blocked from write")
    class ViewerBlockedFromWrite {

        @Test
        void viewer_cannotCreateTicket_returns403() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Viewer should not create")
                    .priority(Priority.LOW)
                    .customerId("cust1")
                    .build();
            ResponseEntity<String> resp = asViewer().postForEntity(baseUrl(), create, String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void viewer_canGetTicket_returns200() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Ticket for viewer read")
                    .priority(Priority.MEDIUM)
                    .customerId("cust1")
                    .build();
            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(baseUrl(), create, TicketResponse.class);
            UUID ticketId = createResp.getBody().getId();

            ResponseEntity<TicketResponse> getResp = asViewer().getForEntity(baseUrl() + "/" + ticketId, TicketResponse.class);
            assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResp.getBody().getId()).isEqualTo(ticketId);
        }

        @Test
        void viewer_cannotUpdateStatus_returns403() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Ticket for viewer status")
                    .priority(Priority.LOW)
                    .customerId("cust1")
                    .build();
            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(baseUrl(), create, TicketResponse.class);
            UUID ticketId = createResp.getBody().getId();

            StatusUpdateRequest statusReq = StatusUpdateRequest.builder().status(TicketStatus.IN_PROGRESS).build();
            ResponseEntity<String> resp = asViewer().exchange(
                    baseUrl() + "/" + ticketId + "/status",
                    HttpMethod.POST,
                    new HttpEntity<>(statusReq, jsonHeaders()),
                    String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void viewer_cannotPatchTicket_returns403() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Ticket for viewer patch")
                    .priority(Priority.LOW)
                    .customerId("cust1")
                    .build();
            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(baseUrl(), create, TicketResponse.class);
            UUID ticketId = createResp.getBody().getId();

            ResponseEntity<String> resp = asViewer().exchange(
                    baseUrl() + "/" + ticketId,
                    HttpMethod.PATCH,
                    new HttpEntity<>(TicketUpdateRequest.builder().title("Viewer update").build(), jsonHeaders()),
                    String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void viewer_cannotAddComment_returns403() {
            TicketCreateRequest create = TicketCreateRequest.builder()
                    .title("Ticket for viewer comment")
                    .priority(Priority.LOW)
                    .customerId("cust1")
                    .build();
            ResponseEntity<TicketResponse> createResp = asAgent().postForEntity(baseUrl(), create, TicketResponse.class);
            UUID ticketId = createResp.getBody().getId();

            CommentCreateRequest commentReq = CommentCreateRequest.builder().comment("Viewer comment").author("viewer").build();
            ResponseEntity<String> resp = asViewer().postForEntity(
                    baseUrl() + "/" + ticketId + "/comments",
                    commentReq,
                    String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
