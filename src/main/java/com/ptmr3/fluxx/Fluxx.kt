package com.ptmr3.fluxx

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Fluxx {
    private val mActionSubscribers = ConcurrentHashMap<Any, Set<Method>>()
    private val mReactionSubscribers = ConcurrentHashMap<Any, Set<Method>>()

    fun getActionSubscriberMethods(action: Action): Observable<HashMap<String, Any>> {
        return Observable.create { observableEmitter ->
            mActionSubscribers.keys.map { parentClass ->
                mActionSubscribers[parentClass].orEmpty().map {
                    if (action.type == it.getAnnotation(com.ptmr3.fluxx.annotation.Action::class.java).actionType) {
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

    fun getReactionSubscriberMethods(reaction: Reaction): Observable<HashMap<String, Any>> {
        return Observable.create { observableEmitter ->
            mReactionSubscribers.keys.map { parentClass ->
                mReactionSubscribers[parentClass].orEmpty().map {
                    if (reaction.type == it.getAnnotation(com.ptmr3.fluxx.annotation.Reaction::class.java).reactionType) {
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

    fun getFailureReactionSubscriberMethods(reaction: Reaction): Observable<HashMap<String, Any>> {
        return Observable.create { observableEmitter ->
            mReactionSubscribers.keys.map { parentClass ->
                mReactionSubscribers[parentClass].orEmpty().map {
                    if (reaction.type == it.getAnnotation(com.ptmr3.fluxx.annotation.FailureReaction::class.java).reactionType) {
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
                    if (it.isAnnotationPresent(com.ptmr3.fluxx.annotation.Action::class.java) && paramTypes.size == 1 && paramTypes[0] == Action::class.java) {
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
                    if (it.isAnnotationPresent(com.ptmr3.fluxx.annotation.Reaction::class.java)) {
                        classMethods.add(it)
                    }
                }
                mReactionSubscribers[parentClass] = classMethods
            }
        }
    }

    private fun methodsWithFailureReactionAnnotation(parentClass: Any): Completable {
        return Completable.fromAction {
            if (!mReactionSubscribers.containsKey(parentClass)) {
                val classMethods = HashSet<Method>()
                parentClass.javaClass.declaredMethods.map {
                    if (it.isAnnotationPresent(com.ptmr3.fluxx.annotation.FailureReaction::class.java)) {
                        classMethods.add(it)
                    }
                }
                mReactionSubscribers[parentClass] = classMethods
            }
        }
    }

    fun registerActionSubscriber(storeClass: Any) {
        if (storeClass is Store) {
            methodsWithActionAnnotation(storeClass).subscribeOn(Schedulers.newThread()).subscribe()
        }
    }

    fun registerReactionSubscriber(viewClass: Any) {
        methodsWithReactionAnnotation(viewClass).subscribeOn(Schedulers.newThread()).subscribe()
        methodsWithFailureReactionAnnotation(viewClass).subscribeOn(Schedulers.newThread()).subscribe()
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