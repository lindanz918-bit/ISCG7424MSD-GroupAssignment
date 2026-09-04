package com.groupproject.justeat.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Rule;
import org.junit.Test;

public class BaseViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void startsIdleAndWithoutError() {
        TestViewModel viewModel = new TestViewModel();

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
    }

    @Test
    public void protectedSettersPublishState() {
        TestViewModel viewModel = new TestViewModel();

        viewModel.updateLoading(true);
        viewModel.updateError("Network unavailable");

        assertEquals(Boolean.TRUE, viewModel.getLoading().getValue());
        assertEquals("Network unavailable", viewModel.getErrorMessage().getValue());
    }

    private static final class TestViewModel extends BaseViewModel {
        void updateLoading(boolean value) {
            setLoading(value);
        }

        void updateError(String value) {
            setErrorMessage(value);
        }
    }
}
