package com.ctu.service;

import com.ctu.exception.PaymentFailedException;
import com.ctu.exception.RoomNotAvailableException;
import com.ctu.model.BookingReceipt;
import com.ctu.model.Room;
import com.ctu.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Test Suite - Bài tập 1")
class BookingServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private BookingService bookingService;

    // =========================================================================
    // 1. Luồng thành công (Success Flow)
    // =========================================================================
    @Test
    @DisplayName("testBookRoom_Success: Đặt phòng thành công, phòng chuyển sang unavailable, gọi save và charge đúng 1 lần")
    void testBookRoom_Success() {
        // 1. ARRANGE
        String roomId = "R101";
        int nights = 3;
        double pricePerNight = 500000.0;
        double expectedTotal = pricePerNight * nights; // 1,500,000.0
        String creditCard = "1234-5678-9012-3456";

        Room availableRoom = new Room(roomId, pricePerNight, true);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(paymentGateway.charge(creditCard, expectedTotal)).thenReturn(true);

        // 2. ACT
        BookingReceipt receipt = bookingService.bookRoom(roomId, nights, creditCard);

        // 3. ASSERT
        assertNotNull(receipt, "Biên lai đặt phòng không được null");
        assertNotNull(receipt.getBookingId(), "Mã biên lai không được null");
        assertFalse(receipt.getBookingId().trim().isEmpty(), "Mã biên lai không được rỗng");
        assertEquals(roomId, receipt.getRoomId(), "Mã phòng trong biên lai phải khớp");
        assertEquals(expectedTotal, receipt.getTotalAmount(), 0.001, "Tổng tiền thanh toán phải bằng đơn giá nhân số đêm");
        assertFalse(availableRoom.isAvailable(), "Trạng thái phòng phải chuyển sang false sau khi đặt thành công");

        // Verification: Kiểm tra số lần gọi hàm
        verify(roomRepository, times(1)).findById(roomId);
        verify(paymentGateway, times(1)).charge(creditCard, expectedTotal);
        verify(roomRepository, times(1)).save(availableRoom);
    }

    // =========================================================================
    // 2. Kiểm thử Ngoại lệ Nghiệp vụ (Business Exceptions)
    // =========================================================================
    @Test
    @DisplayName("testBookRoom_RoomNotFound_ThrowsException: Ném RoomNotAvailableException khi không tìm thấy phòng trong DB")
    void testBookRoom_RoomNotFound_ThrowsException() {
        // 1. ARRANGE
        String roomId = "R999";
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // 2. ACT & 3. ASSERT
        RoomNotAvailableException exception = assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.bookRoom(roomId, 2, "1234-5678-9012-3456");
        });

        assertEquals("Khong tim thay phong: " + roomId, exception.getMessage());
        verify(roomRepository, times(1)).findById(roomId);
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("testBookRoom_RoomAlreadyBooked_ThrowsException: Ném RoomNotAvailableException khi phòng đã có người đặt trước (isAvailable == false)")
    void testBookRoom_RoomAlreadyBooked_ThrowsException() {
        // 1. ARRANGE
        String roomId = "R102";
        Room bookedRoom = new Room(roomId, 400000.0, false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(bookedRoom));

        // 2. ACT & 3. ASSERT
        RoomNotAvailableException exception = assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.bookRoom(roomId, 2, "1234-5678-9012-3456");
        });

        assertEquals("Phong da duoc dat truoc do: " + roomId, exception.getMessage());
        verify(roomRepository, times(1)).findById(roomId);
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("testBookRoom_PaymentFailed_ThrowsException: Ném PaymentFailedException khi thanh toán qua cổng PaymentGateway trả về false")
    void testBookRoom_PaymentFailed_ThrowsException() {
        // 1. ARRANGE
        String roomId = "R103";
        int nights = 2;
        double pricePerNight = 600000.0;
        double totalAmount = pricePerNight * nights; // 1,200,000.0
        String creditCard = "1234-5678-9012-3456";

        Room availableRoom = new Room(roomId, pricePerNight, true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(paymentGateway.charge(creditCard, totalAmount)).thenReturn(false);

        // 2. ACT & 3. ASSERT
        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () -> {
            bookingService.bookRoom(roomId, nights, creditCard);
        });

        assertEquals("Thanh toan qua the credit card that bai!", exception.getMessage());
        assertTrue(availableRoom.isAvailable(), "Phòng vẫn phải giữ trạng thái available = true khi thanh toán thất bại");
        verify(roomRepository, times(1)).findById(roomId);
        verify(paymentGateway, times(1)).charge(creditCard, totalAmount);
        verify(roomRepository, never()).save(any());
    }

    // =========================================================================
    // 3. Kiểm thử Tham số Biên & Rẽ nhánh (Boundary & Edge Cases)
    // =========================================================================
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("testBookRoom_InvalidNights_ThrowsException: Ném IllegalArgumentException khi nights <= 0")
    void testBookRoom_InvalidNights_ThrowsException(int nights) {
        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.bookRoom("R101", nights, "1234-5678-9012-3456");
        });

        assertEquals("Tham so dat phong khong hop le!", exception.getMessage());
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(paymentGateway);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("testBookRoom_NullOrEmptyCreditCard_ThrowsException: Ném IllegalArgumentException khi thẻ tín dụng null hoặc rỗng/khoảng trắng")
    void testBookRoom_NullOrEmptyCreditCard_ThrowsException(String creditCard) {
        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.bookRoom("R101", 2, creditCard);
        });

        assertEquals("Tham so dat phong khong hop le!", exception.getMessage());
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("testBookRoom_NullRoomId_ThrowsException: Ném IllegalArgumentException khi roomId bị null")
    void testBookRoom_NullRoomId_ThrowsException() {
        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.bookRoom(null, 2, "1234-5678-9012-3456");
        });

        assertEquals("Tham so dat phong khong hop le!", exception.getMessage());
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(paymentGateway);
    }
}
