package com.ctu.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DiscountCalculator Test Suite")
class DiscountCalculatorTest {

    private DiscountCalculator calculator;

    @BeforeEach
    void setUp() {
        // 1. ARRANGE (Dùng chung cho các bài test)
        calculator = new DiscountCalculator();
    }

    @Test
    @DisplayName("Tinh gia chiet khau 20% cho san pham 100k thanh cong")
    void testCalculateDiscountPrice_Success() {
        // 2. ACT
        double actualPrice = calculator.calculateDiscountPrice(100000, 20);

        // 3. ASSERT
        assertEquals(80000, actualPrice, 0.001, "Gia sau chiet khau 20% cua 100k phai la 80k");
    }

    @Test
    @DisplayName("Nem IllegalArgumentException khi phan tram chiet khau vuot qua 100%")
    void testCalculateDiscountPrice_InvalidDiscount_ThrowsException() {
        // ACT & ASSERT Ngoai le
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateDiscountPrice(100000, 150);
        });
        assertEquals("Gia hoac phan tram chiet khau khong hop le!", exception.getMessage());
    }

    @Test
    @DisplayName("Nem IllegalArgumentException khi gia goc am (< 0)")
    void testCalculateDiscountPrice_NegativePrice_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateDiscountPrice(-10000, 20);
        });
        assertEquals("Gia hoac phan tram chiet khau khong hop le!", exception.getMessage());
    }

    @Test
    @DisplayName("Nem IllegalArgumentException khi phan tram chiet khau am (< 0)")
    void testCalculateDiscountPrice_NegativeDiscount_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateDiscountPrice(100000, -5);
        });
        assertEquals("Gia hoac phan tram chiet khau khong hop le!", exception.getMessage());
    }

    @ParameterizedTest
    @DisplayName("Kiểm thử tham số hóa với nhiều mức giá và % chiết khấu")
    @CsvSource({
        "100000, 0, 100000",
        "200000, 50, 100000",
        "500000, 10, 450000"
    })
    void testCalculateDiscountPrice_Parameterized(double price, double percent, double expected) {
        assertEquals(expected, calculator.calculateDiscountPrice(price, percent), 0.001);
    }
}
