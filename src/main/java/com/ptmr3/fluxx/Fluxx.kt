package com.ptmr3.fluxx

import com.ptmr3.fluxx.annotation.Action
import com.ptmr3.fluxx.annotation.Reaction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.xml.transform.OutputKeys.METHOD
import android.R.attr.entries
import android.app.Application
import android.util.Log
import dalvik.system.DexFile
import dalvik.system.PathClassLoader
import java.io.IOException


class Fluxx(private val mApplication: Application) {
    private val mActionSubscribers = ConcurrentHashMap<Any, Set<Method>>()
    private val mReactionSubscribers = ConcurrentHashMap<Any, Set<Method>>()

    init {
        sInstance = this
        getClassesOfPackage()
    }

    private fun getClassesOfPackage() {
        try {
            val classLoader = mApplication.classLoader as PathClassLoader
            val packageCodePath = mApplication.packageCodePath
            val df = DexFile(packageCodePath)
            val iter = df.entries()
            df.entries().toList().map {

                val className = iter.nextElement()
                if (className.contains(mApplication.packageName)) {
                    val clazz = classLoader.loadClass(className)
                    val superclassName = clazz.superclass?.simpleName.toString()
                    if (superclassName.contains("FluxxStore")) {
                        registerReactionSubscriber(clazz)
                    }
                    clazz.interfaces.filter {it.simpleName.toString().contains("Fluxx")}
                                .map {
                                    if (it.simpleName.toString().contains("FluxxReactionSubscriber")) {
                                        registerReactionSubscriber(clazz)
                                    }
                                    if (it.simpleName.toString().contains("FluxxStore")) {
                                        registerActionSubscriber(clazz)
                                    }
                                }
                    }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

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
                    val paramTypes = it.parameterTypes
                    if (it.isAnnotationPresent(Reaction::class.java) && paramTypes.size == 1 && paramTypes[0] == FluxxReaction::class.java) {
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
            Log.i("SCAN", storeClass.toString())
        }
    }

    fun registerReactionSubscriber(viewClass: Any) {
        methodsWithReactionAnnotation(viewClass).subscribeOn(Schedulers.newThread()).subscribe()
        Log.i("SCAN", viewClass.toString())
    }

    fun unregisterReactionSubscriber(view: Any) {
        if (mReactionSubscribers.containsKey(view)) {
            mReactionSubscribers.remove(view)
        }
    }

    companion object {
        var sInstance: Fluxx? = null
            get() = field?.let { it }
            private set
        const val ACTION = "action"
        const val CLASS = "class"
        const val REACTION = "reaction"
    }
}