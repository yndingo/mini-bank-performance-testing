package com.example.minibank.operation;

/*
 * Состояния двухэтапного перевода.
 *
 * PENDING — подготовлен, деньги не перемещались.
 * COMPLETED — перевод выполнен.
 *
 * Просроченность определяется отдельно по expiresAt.
 */

public enum TransferSessionStatus {
    PENDING,
    COMPLETED
}
