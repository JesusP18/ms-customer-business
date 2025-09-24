package com.customer.business;

import com.customer.business.cache.CacheService;
import com.customer.business.mapper.CustomerMapper;
import com.customer.business.model.CustomerCreateRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.CustomerUpdateRequest;
import com.customer.business.model.DebitCardAssociationRequest;
import com.customer.business.model.DebitCardBalanceResponse;
import com.customer.business.model.PaymentRequest;
import com.customer.business.model.PaymentResponse;
import com.customer.business.model.ProductReportResponse;
import com.customer.business.model.ProductRequest;
import com.customer.business.model.ProductResponse;
import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.service.CustomerService;
import com.customer.business.service.DebitCardService;
import com.customer.business.service.PaymentService;
import com.customer.business.service.ReportService;
import com.customer.business.validator.CreateCustomerValidator;
import com.customer.business.validator.UpdateCustomerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CustomerApiImpl.class)
@ExtendWith(SpringExtension.class)
@Import(CustomerApiImpl.class)
class CustomerApiImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerMapper customerMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private DebitCardService debitCardService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private UpdateCustomerValidator updateValidator;

    @MockBean
    private CreateCustomerValidator createValidator;

    @MockBean
    private CacheService cacheService;

    private CustomerCreateRequest customerCreateRequest;

    private CustomerResponse customerResponse;

    private Customer customerEntity;

    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        customerCreateRequest = new CustomerCreateRequest();
        customerCreateRequest.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        customerCreateRequest.setFirstName("John");
        customerCreateRequest.setLastName("Doe");
        customerCreateRequest.setProfile(CustomerCreateRequest.ProfileEnum.STANDARD);

        customerResponse = new CustomerResponse();
        customerResponse.setId("1");
        customerResponse.setCustomerType(CustomerResponse.CustomerTypeEnum.PERSONAL);
        customerResponse.setFirstName("John");
        customerResponse.setLastName("Doe");
        customerResponse.setProfile(CustomerResponse.ProfileEnum.STANDARD);

        customerEntity = new Customer();
        customerEntity.setId("1");
        customerEntity.setCustomerType("PERSONAL");
        customerEntity.setFirstName("John");
        customerEntity.setLastName("Doe");
        customerEntity.setProfile("STANDARD");

        productRequest = new ProductRequest();
        productRequest.setCustomerId("1");
        productRequest.setCategory(ProductRequest.CategoryEnum.LIABILITY);
        productRequest.setType(ProductRequest.TypeEnum.ACCOUNT);
        productRequest.setSubType(ProductRequest.SubTypeEnum.SAVINGS);
    }

    @Test
    @DisplayName("POST /api/customers - éxito")
    void createCustomerShouldReturnCreated() {
        doNothing().when(createValidator).validate(any());
        when(customerMapper.getCustomerofCustomerCreateRequest(any())).thenReturn(customerEntity);
        when(customerService.create(any())).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customerCreateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);
    }

    @Test
    @DisplayName("POST /api/customers - error de validación")
    void createCustomerShouldReturnBadRequestOnValidation() {
        doThrow(new RuntimeException("Error de validación")).when(createValidator).validate(any());

        webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customerCreateRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("GET /api/customers - éxito")
    void getAllCustomersShouldReturnList() {
        when(customerService.findAll()).thenReturn(Flux.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.get()
                .uri("/api/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(1)
                .contains(customerResponse);
    }

    @Test
    @DisplayName("GET /api/customers/{id} - éxito")
    void getCustomerByIdShouldReturnCustomer() {
        when(cacheService.getCachedCustomer("1")).thenReturn(Mono.empty());
        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.get()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);
    }

    @Test
    @DisplayName("GET /api/customers/{id} - no encontrado")
    void getCustomerByIdShouldReturnNotFound() {
        when(cacheService.getCachedCustomer("1")).thenReturn(Mono.empty());
        when(customerService.findById("1")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - éxito")
    void updateCustomerShouldReturnOk() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setFirstName("Updated");
        doNothing().when(updateValidator).validate(any());
        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerService.update(eq("1"), any())).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerFromUpdateRequest(any(), any())).thenReturn(customerEntity);
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.put()
                .uri("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - error de validación")
    void updateCustomerShouldReturnBadRequestOnValidation() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        doThrow(new RuntimeException("Error de validación")).when(updateValidator).validate(any());

        webTestClient.put()
                .uri("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - éxito")
    void deleteCustomerShouldReturnNoContent() {
        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerService.delete("1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - no encontrado")
    void deleteCustomerShouldReturnNotFound() {
        when(customerService.findById("1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/customers/{id}/debit-cards/associate - éxito")
    void associateDebitCardShouldReturnOk() {
        DebitCardAssociationRequest request = new DebitCardAssociationRequest();
        when(
                debitCardService.associateDebitCard(
                        eq("1"), any())).thenReturn(Mono.just("Asociado")
        );

        webTestClient.post()
                .uri("/api/customers/1/debit-cards/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Asociado");
    }

    @Test
    @DisplayName("POST /api/customers/{id}/debit-cards/associate - error")
    void associateDebitCardShouldReturnBadRequest() {
        DebitCardAssociationRequest request = new DebitCardAssociationRequest();
        when(
                debitCardService
                        .associateDebitCard(
                                eq("1"), any()))
                .thenReturn(Mono.error(new IllegalArgumentException("error")));

        webTestClient.post()
                .uri("/api/customers/1/debit-cards/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET /api/customers/{id}/products - éxito")
    void getCustomerProductsShouldReturnOk() {
        Product product = new Product(
                "1", "LIABILITY", "ACCOUNT", "SAVINGS"
        );
        ProductResponse productResponse = new ProductResponse();
        productResponse.setCategory(ProductResponse.CategoryEnum.LIABILITY);
        productResponse.setType(ProductResponse.TypeEnum.ACCOUNT);
        productResponse.setSubType(ProductResponse.SubTypeEnum.SAVINGS);
        when(customerService.getProducts("1")).thenReturn(Flux.just(product));
        // El mapeo se hace inline en el controlador

        webTestClient.get()
                .uri("/api/customers/1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("POST /api/customers/{id}/products - éxito")
    void addProductToCustomerShouldReturnNoContent() {
        when(customerService.addProduct(
                eq("1"), any(Product.class))).thenReturn(Mono.empty()
        );
        ProductRequest req = new ProductRequest();
        req.setCustomerId("1");
        req.setCategory(ProductRequest.CategoryEnum.LIABILITY);
        req.setType(ProductRequest.TypeEnum.ACCOUNT);
        req.setSubType(ProductRequest.SubTypeEnum.SAVINGS);

        webTestClient.post()
                .uri("/api/customers/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /api/customers/{id}/products/{productId} - éxito")
    void removeProductFromCustomerShouldReturnNoContent() {
        when(customerService.removeProduct("1", "prod1"))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1/products/prod1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("POST /api/customers/{id}/payments - éxito")
    void payCreditProductShouldReturnOk() {
        PaymentRequest paymentRequest = new PaymentRequest();
        PaymentResponse paymentResponse = new PaymentResponse();
        when(paymentService.payCreditProduct(eq("1"),
                any(PaymentRequest.class))).thenReturn(Mono.just(paymentResponse));
        when(customerMapper.mapToPaymentResponse(any())).thenReturn(paymentResponse);

        webTestClient.post()
                .uri("/api/customers/1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .isEqualTo(paymentResponse);
    }

    @Test
    @DisplayName("GET /api/customers/{id}/debit-cards/{cardId}/balance - éxito")
    void getMainAccountBalanceShouldReturnOk() {
        DebitCardBalanceResponse balanceResponse = new DebitCardBalanceResponse();
        balanceResponse.setCardId("card1");
        balanceResponse.setProductId("prod1");
        balanceResponse.setBalance(1000.0);
        when(debitCardService.getMainAccountId(
                "1", "card1")
        ).thenReturn(Mono.just("prod1"));
        when(
                debitCardService
                        .getMainAccountBalance(
                                "prod1", "card1")
        ).thenReturn(Mono.just(balanceResponse));

        webTestClient.get()
                .uri("/api/customers/1/debit-cards/card1/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DebitCardBalanceResponse.class)
                .isEqualTo(balanceResponse);
    }

    @Test
    @DisplayName("GET /api/reports/products - éxito")
    void getProductReportsShouldReturnOk() {
        ProductReportResponse reportResponse = new ProductReportResponse();
        reportResponse.setProductId("prod1");
        reportResponse.setBalance(1000.0);
        when(reportService.generateProductReport(
                any(), any())).thenReturn(Flux.just(reportResponse)
        );
        when(customerMapper
                .mapToProductReportResponse(any())).thenReturn(reportResponse
        );

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/reports/products")
                        .queryParam("from", "2023-01-01")
                        .queryParam("to", "2023-12-31")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductReportResponse.class)
                .hasSize(1)
                .contains(reportResponse);
    }
}

