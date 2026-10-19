package com.ctu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model and DTO Unit Tests")
class ModelTest {

    @Test
    @DisplayName("Kiểm tra khởi tạo và các phương thức getter/setter của Room")
    void testRoomModel() {
        Room room = new Room("R201", 350000.0, true);
        assertEquals("R201", room.getRoomId());
        assertEquals(350000.0, room.getPricePerNight(), 0.001);
        assertTrue(room.isAvailable());

        room.setAvailable(false);
        assertFalse(room.isAvailable());
    }

    @Test
    @DisplayName("Kiểm tra khởi tạo và các phương thức getter của BookingReceipt")
    void testBookingReceiptModel() {
        BookingReceipt receipt = new BookingReceipt("BK-001", "R201", 700000.0);
        assertEquals("BK-001", receipt.getBookingId());
        assertEquals("R201", receipt.getRoomId());
        assertEquals(700000.0, receipt.getTotalAmount(), 0.001);
    }
}
