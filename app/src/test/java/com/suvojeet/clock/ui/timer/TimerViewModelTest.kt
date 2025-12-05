package com.suvojeet.clock.ui.timer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {
    
    private lateinit var viewModel: TimerViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TimerViewModel()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have zero time`() {
        assertEquals(0L, viewModel.timeLeft.value)
        assertEquals(0L, viewModel.totalTime.value)
    }
    
    @Test
    fun `initial state should not be running`() {
        assertFalse(viewModel.isRunning.value)
    }
    
    @Test
    fun `setTimer should set correct duration`() {
        viewModel.setTimer(1, 30, 45) // 1 hour, 30 min, 45 sec
        
        val expectedMillis = ((1 * 3600) + (30 * 60) + 45) * 1000L
        assertEquals(expectedMillis, viewModel.totalTime.value)
        assertEquals(expectedMillis, viewModel.timeLeft.value)
    }
    
    @Test
    fun `setTimer should set correct duration for minutes only`() {
        viewModel.setTimer(0, 5, 0) // 5 minutes
        
        val expectedMillis = (5 * 60) * 1000L
        assertEquals(expectedMillis, viewModel.totalTime.value)
        assertEquals(expectedMillis, viewModel.timeLeft.value)
    }
    
    @Test
    fun `setTimer should set correct duration for seconds only`() {
        viewModel.setTimer(0, 0, 30) // 30 seconds
        
        val expectedMillis = 30 * 1000L
        assertEquals(expectedMillis, viewModel.totalTime.value)
        assertEquals(expectedMillis, viewModel.timeLeft.value)
    }
    
    @Test
    fun `startTimer should not start when time is zero`() = runTest {
        viewModel.startTimer()
        assertFalse(viewModel.isRunning.value)
    }
    
    @Test
    fun `startTimer should start when time is set`() = runTest {
        viewModel.setTimer(0, 1, 0)
        viewModel.startTimer()
        assertTrue(viewModel.isRunning.value)
    }
    
    @Test
    fun `pauseTimer should stop the timer`() = runTest {
        viewModel.setTimer(0, 1, 0)
        viewModel.startTimer()
        viewModel.pauseTimer()
        assertFalse(viewModel.isRunning.value)
    }
    
    @Test
    fun `resetTimer should restore time to total time`() = runTest {
        viewModel.setTimer(0, 5, 0)
        val expectedTotal = viewModel.totalTime.value
        
        // Simulate some time passing (manually decrease time)
        viewModel.startTimer()
        viewModel.pauseTimer()
        
        viewModel.resetTimer()
        assertEquals(expectedTotal, viewModel.timeLeft.value)
    }
    
    @Test
    fun `stopTimer should reset everything to zero`() = runTest {
        viewModel.setTimer(0, 5, 0)
        viewModel.startTimer()
        viewModel.stopTimer()
        
        assertEquals(0L, viewModel.timeLeft.value)
        assertEquals(0L, viewModel.totalTime.value)
        assertFalse(viewModel.isRunning.value)
    }
}
