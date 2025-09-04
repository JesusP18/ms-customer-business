package com.customer.business;

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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = CustomerApiImpl.class)
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

    private CustomerCreateRequest customerCreateRequest;

    private CustomerResponse customerResponse;

    private Customer customerEntity;

    private ProductRequest productRequest;

    @MockBean
    private UpdateCustomerValidator updateValidator;

    @MockBean
    private CreateCustomerValidator createValidator;

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
    void createCustomerShouldReturnCreated() {
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

        verify(customerService).create(any());
    }

    @Test
    void getAllCustomersShouldReturnCustomers() {
        when(customerService.findAll()).thenReturn(Flux.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.get()
                .uri("/api/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(1)
                .contains(customerResponse);

        verify(customerService).findAll();
    }

    @Test
    void getCustomerByIdShouldReturnCustomer() {
        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.get()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);

        verify(customerService).findById("1");
    }

    @Test
    void getCustomerByIdShouldReturnNotFound() {
        when(customerService.findById("1")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNotFound();

        verify(customerService).findById("1");
    }

    @Test
    void updateCustomerShouldReturnUpdatedCustomer() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setFirstName("UpdatedName");

        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerService.update(eq("1"), any())).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.put()
                .uri("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);

        verify(customerService).update(eq("1"), any());
    }

    @Test
    void deleteCustomerShouldReturnNoContent() {
        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerService.delete("1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(customerService).delete("1");
    }

    @Test
    void getCustomerProductsShouldReturnProducts() {
        Product product = new Product("1", "LIABILITY", "ACCOUNT", "SAVINGS");
        ProductResponse productResponse = new ProductResponse();
        productResponse.setCategory(ProductResponse.CategoryEnum.LIABILITY);
        productResponse.setType(ProductResponse.TypeEnum.ACCOUNT);
        productResponse.setSubType(ProductResponse.SubTypeEnum.SAVINGS);

        when(customerService.getProducts("1")).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/customers/1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .hasSize(1);

        verify(customerService).getProducts("1");
    }

    @Test
    void getProductReportsShouldReturnReports() {
        ProductReportResponse reportResponse = new ProductReportResponse();
        reportResponse.setProductId("prod1");
        reportResponse.setBalance(1000.0);

        when(reportService.generateProductReport(
                any(), any())).thenReturn(Flux.just(reportResponse)
        );
        when(customerMapper.mapToProductReportResponse(
                any())).thenReturn(reportResponse
        );

        webTestClient.get()
                .uri(
                        "/api/reports/products?from=2023-01-01&to=2023-12-31"
                )
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductReportResponse.class)
                .hasSize(1);

        verify(reportService).generateProductReport(any(), any());
    }

    @Test
    void addProductToCustomerShouldReturnNoContent() {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setCustomerId("1");
        productRequest.setCategory(ProductRequest.CategoryEnum.LIABILITY);
        productRequest.setType(ProductRequest.TypeEnum.ACCOUNT);
        productRequest.setSubType(ProductRequest.SubTypeEnum.SAVINGS);

        when(customerService.addProduct(eq("1"), any(Product.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/customers/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequest)
                .exchange()
                .expectStatus().isNoContent();

        verify(customerService).addProduct(eq("1"), any(Product.class));
    }

    @Test
    void removeProductFromCustomerShouldReturnNoContent() {
        when(customerService.removeProduct("1", "prod1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1/products/prod1")
                .exchange()
                .expectStatus().isNoContent();

        verify(customerService).removeProduct("1", "prod1");
    }

    @Test
    void payCreditProductShouldReturnOk() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTargetProductId("prod1");
        paymentRequest.setAmount(100.0);

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("SUCCESS");
        paymentResponse.setMessage("Payment processed");

        when(paymentService.payCreditProduct(
                eq("1"), any(PaymentRequest.class)
        )).thenReturn(Mono.just(paymentResponse));
        when(customerMapper.mapToPaymentResponse(
                any(PaymentResponse.class)
        )).thenReturn(paymentResponse);

        webTestClient.post()
                .uri("/api/customers/1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .isEqualTo(paymentResponse);

        verify(paymentService).payCreditProduct(eq("1"), any(PaymentRequest.class));
    }

    @Test
    void associateDebitCardShouldReturnOk() {
        DebitCardAssociationRequest request = new DebitCardAssociationRequest();
        request.setCardId("card1");
        request.setAccountIds(List.of("acc1", "acc2"));

        when(debitCardService.associateDebitCard(
                eq("1"), any(DebitCardAssociationRequest.class)
        )).thenReturn(Mono.just("Associated"));

        webTestClient.post()
                .uri("/api/customers/1/debit-cards/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Associated");

        verify(debitCardService).associateDebitCard(
                eq("1"), any(DebitCardAssociationRequest.class)
        );
    }

    @Test
    void getMainAccountBalanceShouldReturnOk() {
        DebitCardBalanceResponse balanceResponse = new DebitCardBalanceResponse();
        balanceResponse.setCardId("card1");
        balanceResponse.setProductId("prod1");
        balanceResponse.setBalance(1000.0);

        when(debitCardService.getMainAccountId("1", "card1")).thenReturn(Mono.just("prod1"));
        when(debitCardService.getMainAccountBalance(
                "prod1", "card1")
        ).thenReturn(Mono.just(balanceResponse));

        webTestClient.get()
                .uri("/api/customers/1/debit-cards/card1/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DebitCardBalanceResponse.class)
                .isEqualTo(balanceResponse);

        verify(debitCardService).getMainAccountId("1", "card1");
        verify(debitCardService).getMainAccountBalance("prod1", "card1");
    }

    @Test
    void getProductReportsShouldReturnOk() {
        ProductReportResponse reportResponse = new ProductReportResponse();
        reportResponse.setProductId("prod1");
        reportResponse.setType("ACCOUNT");
        reportResponse.setSubType("SAVINGS");
        reportResponse.setBalance(1000.0);

        when(reportService.generateProductReport(
                any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(Flux.just(reportResponse));
        when(customerMapper.mapToProductReportResponse(
                any(ProductReportResponse.class))
        ).thenReturn(reportResponse);

        webTestClient.get()
                .uri("/api/reports/products?from=2023-01-01&to=2023-12-31")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductReportResponse.class)
                .hasSize(1)
                .contains(reportResponse);

        verify(reportService).generateProductReport(any(LocalDate.class), any(LocalDate.class));
    }
}