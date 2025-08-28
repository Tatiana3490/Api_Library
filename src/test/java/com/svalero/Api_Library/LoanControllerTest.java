package com.svalero.Api_Library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.Api_Library.DTO.LoanDTO;
import com.svalero.Api_Library.controller.LoanController;
import com.svalero.Api_Library.domain.Loan;
import com.svalero.Api_Library.exception.LoanNotFoundException;
import com.svalero.Api_Library.security.JwtAuthenticationFilter;
import com.svalero.Api_Library.security.JwtRequestFilter;
import com.svalero.Api_Library.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = LoanController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtRequestFilter.class, JwtAuthenticationFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class LoanControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean LoanService loanService;

    // Por si algo de seguridad se cuela
    @MockBean JwtRequestFilter jwtRequestFilter;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    // ---------- helpers ----------
    private Loan loan(long id, String customer, LocalDate date, int qty) {
        Loan l = new Loan();
        l.setId(id);
        l.setCustomerName(customer);
        l.setLoanDate(date);
        l.setQuantity(qty);
        return l;
    }
    private LoanDTO dto(long id, String name, String customer, LocalDate date, int qty) {
        LoanDTO d = new LoanDTO();
        d.setId(id);
        d.setName(name);
        d.setCustomerName(customer);
        d.setLoanDate(date);
        d.setQuantity(qty);
        return d;
    }

    // =============== GET lista ===============
    @Test
    @DisplayName("GET /loans -> 200 y lista de DTOs")
    void getAllLoans_Returns200() throws Exception {
        var l1 = loan(1, "Alice", LocalDate.parse("2024-01-10"), 2);
        var l2 = loan(2, "Bob",   LocalDate.parse("2024-02-15"), 1);

        when(loanService.getAllLoans()).thenReturn(List.of(l1, l2));
        when(loanService.convertToDTO(l1)).thenReturn(dto(1, null, "Alice", LocalDate.parse("2024-01-10"), 2));
        when(loanService.convertToDTO(l2)).thenReturn(dto(2, null, "Bob",   LocalDate.parse("2024-02-15"), 1));

        mockMvc.perform(get("/loans").accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Alice"))
                .andExpect(jsonPath("$[1].customerName").value("Bob"));

        verify(loanService).getAllLoans();
        verify(loanService).convertToDTO(l1);
        verify(loanService).convertToDTO(l2);
        verifyNoMoreInteractions(loanService);
    }

    // =============== GET by id ===============
    @Test
    @DisplayName("GET /loans/{id} -> 200")
    void getLoanById_Returns200() throws Exception {
        var l = loan(5, "Carol", LocalDate.parse("2024-03-01"), 3);
        when(loanService.getLoanById(5L)).thenReturn(l);
        when(loanService.convertToDTO(l)).thenReturn(dto(5, null, "Carol", LocalDate.parse("2024-03-01"), 3));

        mockMvc.perform(get("/loans/{id}", 5L).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.customerName").value("Carol"))
                .andExpect(jsonPath("$.loanDate").value("2024-03-01"))
                .andExpect(jsonPath("$.quantity").value(3));

        verify(loanService).getLoanById(5L);
        verify(loanService).convertToDTO(l);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("GET /loans/{id} -> 404 si no existe")
    void getLoanById_Returns404() throws Exception {
        when(loanService.getLoanById(999L)).thenThrow(new LoanNotFoundException("not found"));

        mockMvc.perform(get("/loans/{id}", 999L).accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(loanService).getLoanById(999L);
        verifyNoMoreInteractions(loanService);
    }

    // =============== GET filtros ===============
    @Test
    @DisplayName("GET /loans/customer-name -> 200")
    void getByCustomerName_Returns200() throws Exception {
        var l = loan(1, "Alice", LocalDate.parse("2024-01-10"), 2);
        when(loanService.getLoanByCustomerName("Alice")).thenReturn(List.of(l));
        when(loanService.convertToDTO(l)).thenReturn(dto(1, null, "Alice", LocalDate.parse("2024-01-10"), 2));

        mockMvc.perform(get("/loans/customer-name").queryParam("customerName", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Alice"));

        verify(loanService).getLoanByCustomerName("Alice");
        verify(loanService).convertToDTO(l);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("GET /loans/loan-date -> 200")
    void getByLoanDate_Returns200() throws Exception {
        LocalDate d = LocalDate.parse("2024-01-10");
        var l = loan(2, "Bob", d, 1);
        when(loanService.getLoanByLoanDate(d)).thenReturn(List.of(l));
        when(loanService.convertToDTO(l)).thenReturn(dto(2, null, "Bob", d, 1));

        mockMvc.perform(get("/loans/loan-date").queryParam("loanDate", "2024-01-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanDate").value("2024-01-10"));

        verify(loanService).getLoanByLoanDate(d);
        verify(loanService).convertToDTO(l);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("GET /loans/range -> 200")
    void getLoansBetweenDates_Returns200() throws Exception {
        LocalDate s = LocalDate.parse("2024-01-01");
        LocalDate e = LocalDate.parse("2024-01-31");
        var l = loan(3, "Carol", LocalDate.parse("2024-01-20"), 4);

        when(loanService.getLoansBetweenDates(s, e)).thenReturn(List.of(l));
        when(loanService.convertToDTO(l)).thenReturn(dto(3, null, "Carol", LocalDate.parse("2024-01-20"), 4));

        mockMvc.perform(get("/loans/range")
                        .queryParam("startDate", "2024-01-01")
                        .queryParam("endDate",   "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));

        verify(loanService).getLoansBetweenDates(s, e);
        verify(loanService).convertToDTO(l);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("GET /loans/quantity/eq/{quantity} -> 200")
    void getByQuantity_Returns200() throws Exception {
        var l = loan(7, "Dave", LocalDate.parse("2024-02-02"), 3);
        when(loanService.getLoanByQuantity(3)).thenReturn(List.of(l));
        when(loanService.convertToDTO(l)).thenReturn(dto(7, null, "Dave", LocalDate.parse("2024-02-02"), 3));

        mockMvc.perform(get("/loans/quantity/eq/{quantity}", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(3));

        verify(loanService).getLoanByQuantity(3);
        verify(loanService).convertToDTO(l);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("GET /loans/quantity/native/gt/{min} -> 200")
    void getLoansWithQuantityGreaterThanNative_Returns200() throws Exception {
        var l = loan(8, "Eve", LocalDate.parse("2024-03-03"), 5);
        when(loanService.findLoansWithQuantityGreaterThanNative(4)).thenReturn(List.of(l));
        when(loanService.convertToDTO(l)).thenReturn(dto(8, null, "Eve", LocalDate.parse("2024-03-03"), 5));

        mockMvc.perform(get("/loans/quantity/native/gt/{min}", 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(5));

        verify(loanService).findLoansWithQuantityGreaterThanNative(4);
        verify(loanService).convertToDTO(l);
        verifyNoMoreInteractions(loanService);
    }

    // =============== POST ===============
    @Test
    @DisplayName("POST /loans -> 201 Created")
    void addLoan_Returns201() throws Exception {
        var in  = loan(0, "Alice", LocalDate.parse("2024-01-10"), 2);
        var out = loan(10, "Alice", LocalDate.parse("2024-01-10"), 2);

        when(loanService.saveLoan(any(Loan.class))).thenReturn(out);
        when(loanService.convertToDTO(out)).thenReturn(dto(10, null, "Alice", LocalDate.parse("2024-01-10"), 2));

        mockMvc.perform(post("/loans")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.customerName").value("Alice"));

        verify(loanService).saveLoan(any(Loan.class));
        verify(loanService).convertToDTO(out);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("POST /loans -> 400 Bad Request si faltan datos (usa @NotNull/@Min en entity)")
    void addLoan_Returns400_WhenInvalid() throws Exception {
        // null en customerName y loanDate para activar @NotNull
        var invalid = loan(0, null, null, 0);

        mockMvc.perform(post("/loans")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    // =============== PUT ===============
    @Test
    @DisplayName("PUT /loans/{id} -> 200 OK")
    void updateLoan_Returns200() throws Exception {
        var changes = loan(0, "Alice", LocalDate.parse("2024-01-12"), 3);
        var updated = loan(5, "Alice", LocalDate.parse("2024-01-12"), 3);

        when(loanService.updateLoan(eq(5L), any(Loan.class))).thenReturn(updated);
        when(loanService.convertToDTO(updated)).thenReturn(dto(5, null, "Alice", LocalDate.parse("2024-01-12"), 3));

        mockMvc.perform(put("/loans/{id}", 5L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.loanDate").value("2024-01-12"));

        verify(loanService).updateLoan(eq(5L), any(Loan.class));
        verify(loanService).convertToDTO(updated);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("PUT /loans/{id} -> 404 si no existe")
    void updateLoan_Returns404_WhenNotFound() throws Exception {
        when(loanService.updateLoan(eq(999L), any(Loan.class)))
                .thenThrow(new LoanNotFoundException("no existe"));

        mockMvc.perform(put("/loans/{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loan(0, "X", LocalDate.parse("2024-01-01"), 1))))
                .andExpect(status().isNotFound());

        verify(loanService).updateLoan(eq(999L), any(Loan.class));
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("PUT /loans/{id} -> 400 si datos inválidos (requiere validaciones)")
    void updateLoan_Returns400_WhenInvalid() throws Exception {
        var invalid = loan(0, null, null, 0);

        mockMvc.perform(put("/loans/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoMoreInteractions(loanService);
    }

    // =============== PATCH ===============
    @Test
    @DisplayName("PATCH /loans/{id} -> 200 OK")
    void patchLoan_Returns200() throws Exception {
        var updated = loan(3, "Alice", LocalDate.parse("2024-01-10"), 5);
        when(loanService.updateLoanPartial(eq(3L), anyMap())).thenReturn(updated);
        when(loanService.convertToDTO(updated)).thenReturn(dto(3, null, "Alice", LocalDate.parse("2024-01-10"), 5));

        mockMvc.perform(patch("/loans/{id}", 3L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));

        verify(loanService).updateLoanPartial(eq(3L), anyMap());
        verify(loanService).convertToDTO(updated);
        verifyNoMoreInteractions(loanService);
    }

    // =============== DELETE ===============
    @Test
    @DisplayName("DELETE /loans/{id} -> 204 No Content")
    void deleteLoan_Returns204() throws Exception {
        doNothing().when(loanService).deleteLoan(4L);

        mockMvc.perform(delete("/loans/{id}", 4L))
                .andExpect(status().isNoContent());

        verify(loanService).deleteLoan(4L);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("DELETE /loans/{id} -> 404 si no existe")
    void deleteLoan_Returns404_WhenNotFound() throws Exception {
        doThrow(new LoanNotFoundException("no existe")).when(loanService).deleteLoan(999L);

        mockMvc.perform(delete("/loans/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(loanService).deleteLoan(999L);
        verifyNoMoreInteractions(loanService);
    }
}
