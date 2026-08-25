package com.joven.inventory.context;

import com.joven.inventory.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link StoreContext}.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@DisplayName("StoreContext Tests")
class StoreContextTest {

    @AfterEach
    void tearDown() {
        StoreContext.clear();
    }

    @Test
    @DisplayName("getStoreId - after set - returns value")
    void getStoreId_afterSet_returnsValue() {
        StoreContext.setStoreId(4L);
        assertThat(StoreContext.getStoreId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("requireStoreId - when set - returns value")
    void requireStoreId_whenSet_returnsValue() {
        StoreContext.setStoreId(2L);
        assertThat(StoreContext.requireStoreId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("requireStoreId - when not set - throws BadRequestException")
    void requireStoreId_whenNotSet_throws() {
        StoreContext.clear();
        assertThatThrownBy(StoreContext::requireStoreId)
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No store selected");
    }

    @Test
    @DisplayName("clear - removes the store id")
    void clear_removesStoreId() {
        StoreContext.setStoreId(9L);
        StoreContext.clear();
        assertThat(StoreContext.getStoreId()).isNull();
    }
}
