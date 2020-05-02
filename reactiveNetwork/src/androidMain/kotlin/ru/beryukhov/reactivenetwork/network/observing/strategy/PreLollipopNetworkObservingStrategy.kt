/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ru.beryukhov.reactivenetwork.Connectivity
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

/**
 * Network observing strategy for Android devices before Lollipop (API 20 or lower).
 * Uses Broadcast Receiver.
 */
class PreLollipopNetworkObservingStrategy : NetworkObservingStrategy {
    @ExperimentalCoroutinesApi
    override fun observeNetworkConnectivity(context: Context): Flow<Connectivity> {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        return callbackFlow<Connectivity> {
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent
                ) {
                    offer(Connectivity.create(context))
                }
            }
            context.registerReceiver(receiver, filter)
            awaitClose {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        tryToUnregisterReceiver(context, receiver)
                    }
                }
            }
        }
        /*return Observable.create(object : ObservableOnSubscribe<Connectivity?>() {
            @Throws(Exception::class)
            fun subscribe(emitter: ObservableEmitter<Connectivity?>) {
                val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(
                        context: Context,
                        intent: Intent
                    ) {
                        emitter.onNext(Connectivity.create(context))
                    }
                }
                context.registerReceiver(receiver, filter)
                val disposable: Disposable = disposeInUiThread(object : Action() {
                    fun run() {
                        tryToUnregisterReceiver(context, receiver)
                    }
                })
                emitter.setDisposable(disposable)
            }
        }).defaultIfEmpty(Connectivity.create())*/
    }

    protected fun tryToUnregisterReceiver(
        context: Context,
        receiver: BroadcastReceiver?
    ) {
        try {
            context.unregisterReceiver(receiver)
        } catch (exception: Exception) {
            onError("receiver was already unregistered", exception)
        }
    }

    override fun onError(
        message: String,
        exception: Exception
    ) {
        Log.e(ReactiveNetwork.LOG_TAG, message, exception)
    }

    /*private fun disposeInUiThread(action: Action): Disposable {
        return Disposables.fromAction(object : Action() {
            @Throws(Exception::class)
            fun run() {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    action.run()
                } else {
                    val inner: Scheduler.Worker = AndroidSchedulers.mainThread().createWorker()
                    inner.schedule(Runnable {
                        try {
                            action.run()
                        } catch (e: Exception) {
                            onError("Could not unregister receiver in UI Thread", e)
                        }
                        inner.dispose()
                    })
                }
            }
        })
    }*/
}