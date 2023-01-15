package com.officinasocialeproarpaia.officina_android.base

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel<S, E> : ViewModel() {

    private val sharedFlowState = MutableSharedFlow<S>()
    protected val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun observe(scope: LifecycleCoroutineScope, observer: (S) -> Unit) {
        scope.launch {
            sharedFlowState.collect {
                observer(it)
            }
        }
    }

    fun post(state: S) {
        try {
            viewModelScope.launch {
                sharedFlowState.emit(state)
            }
        } catch (e: Exception) {
            Timber.e(e, "error when trying to post state $state in $this")
        }
    }

    abstract fun send(event: E)
}
