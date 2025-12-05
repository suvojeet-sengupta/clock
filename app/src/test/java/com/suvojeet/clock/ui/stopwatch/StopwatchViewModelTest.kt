package com.suvojeet.clock.ui.stopwatch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopwatchViewModelTest {
    
    private lateinit var viewModel: StopwatchViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StopwatchViewModel()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have zero elapsed time`() {
        assertEquals(0L, viewModel.elapsedTime.value)
    }
    
    @Test
    fun `initial state should not be running`() {
        assertFalse(viewModel.isRunning.value)
    }
    
    @Test
    fun `initial state should have no laps`() {
        assertTrue(viewModel.laps.value.isEmpty())
    }
    
    @Test
    fun `startStopwatch should set isRunning to true`() = runTest {
        viewModel.startStopwatch()
        assertTrue(viewModel.isRunning.value)
    }
    
    @Test
    fun `pauseStopwatch should set isRunning to false`() = runTest {
        viewModel.startStopwatch()
        viewModel.pauseStopwatch()
        assertFalse(viewModel.isRunning.value)
    }
    
    @Test
    fun `resetStopwatch should reset all state`() = runTest {
        viewModel.startStopwatch()
        advanceTimeBy(1000)
        viewModel.lap()
        viewModel.resetStopwatch()
        
        assertEquals(0L, viewModel.elapsedTime.value)
        assertFalse(viewModel.isRunning.value)
        assertTrue(viewModel.laps.value.isEmpty())
    }
    
    @Test
    fun `lap should not add lap when not running`() {
        viewModel.lap()
        assertTrue(viewModel.laps.value.isEmpty())
    }
    
    @Test
    fun `formatLapsForShare should return proper message when no laps`() {
        val shareText = viewModel.formatLapsForShare()
        assertEquals("No laps recorded", shareText)
    }
    
    @Test
    fun `formatLapsForShare should include header when laps exist`() = runTest {
        viewModel.startStopwatch()
        advanceTimeBy(1000)
        viewModel.lap()
        viewModel.pauseStopwatch()
        
        val shareText = viewModel.formatLapsForShare()
        assertTrue(shareText.contains("Stopwatch Lap Times"))
    }
}
