package com.ptmr3.fluxx

import com.ptmr3.fluxx.annotation.Action
import com.ptmr3.fluxx.annotation.Reaction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Fluxx {
    private val mActionSubscribers = ConcurrentHashMap<Any, Set<Method>>()
    private val mReactionSubscribers = ConcurrentHashMap<Any, Set<Method>>()

    fun getActionSubscriberMethods(action: FluxxAction): Observable<HashMap<String, Any>> {
        return Observable.create { observableEmitter ->
            mActionSubscribers.keys.map { parentClass ->
                mActionSubscribers[parentClass].orEmpty().map {
                    if (action.type == it.getAnnotation(Action::class.java).actionType) {
                        val map = HashMap<String, Any>()
                        map[METHOD] = it
                        map[CLASS] = parentClass
                        map[ACTION] = action
                        observableEmitter.onNext(map)
                    }
                }
            }
            observableEmitter.onComplete()
        }
    }

    fun getReactionSubscriberMethods(reaction: FluxxReaction): Observable<HashMap<String, Any>> {
        return Observable.create { observableEmitter ->
            mReactionSubscribers.keys.map { parentClass ->
                mReactionSubscribers[parentClass].orEmpty().map {
                    if (reaction.type == it.getAnnotation(Reaction::class.java).reactionType) {
                        val map = HashMap<String, Any>()
                        map[METHOD] = it
                        map[CLASS] = parentClass
                        map[REACTION] = reaction
                        observableEmitter.onNext(map)
                    }
                }
            }
            observableEmitter.onComplete()
        }
    }

    private fun methodsWithActionAnnotation(parentClass: Any): Completable {
        return Completable.fromAction {
            if (!mActionSubscribers.containsKey(parentClass)) {
                val classMethods = HashSet<Method>()
                parentClass.javaClass.declaredMethods.map {
                    val paramTypes = it.parameterTypes
                    if (it.isAnnotationPresent(Action::class.java) && paramTypes.size == 1 && paramTypes[0] == FluxxAction::class.java) {
                        classMethods.add(it)
                    }
                }
                mActionSubscribers[parentClass] = classMethods
            }
        }
    }

    private fun methodsWithReactionAnnotation(parentClass: Any): Completable {
        return Completable.fromAction {
            if (!mReactionSubscribers.containsKey(parentClass)) {
                val classMethods = HashSet<Method>()
                parentClass.javaClass.declaredMethods.map {
                    if (it.isAnnotationPresent(Reaction::class.java)) {
                        classMethods.add(it)
                    }
                }
                mReactionSubscribers[parentClass] = classMethods
            }
        }
    }

    fun registerActionSubscriber(storeClass: Any) {
        if (storeClass is FluxxStore) {
            methodsWithActionAnnotation(storeClass).subscribeOn(Schedulers.newThread()).subscribe()
        }
    }

    fun registerReactionSubscriber(viewClass: Any) {
        methodsWithReactionAnnotation(viewClass).subscribeOn(Schedulers.newThread()).subscribe()
    }

    fun unregisterReactionSubscriber(view: Any) {
        if (mReactionSubscribers.containsKey(view)) {
            mReactionSubscribers.remove(view)
        }
    }

    companion object {
        val instance: Fluxx by lazy { Fluxx() }
        const val ACTION = "action"
        const val CLASS = "class"
        const val METHOD = "method"
        const val REACTION = "reaction"
    }
}