package com.ptmr3.fluxx

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.ptmr3.fluxx.annotation.Action
import com.ptmr3.fluxx.annotation.Reaction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.xml.transform.OutputKeys.METHOD


class Fluxx(application: Application) : Application.ActivityLifecycleCallbacks {
    private val mActionSubscribers = ConcurrentHashMap<Any, Set<Method>>()
    private val mReactionSubscribers = ConcurrentHashMap<Any, Set<Method>>()

    init {
        sInstance = this
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity is FragmentActivity) {
            registerReactionSubscriber(activity)
            (activity as FluxxView).registerStore()
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentAttached(fragmentManager: FragmentManager, fragment: Fragment, context: Context?) {
                    super.onFragmentAttached(fragmentManager, fragment, context)
                    registerReactionSubscriber(fragment).subscribeOn(Schedulers.newThread()).subscribe()
                }

                override fun onFragmentDetached(fragmentManager: FragmentManager, fragment: Fragment) {
                    mReactionSubscribers.remove(fragment)
                    super.onFragmentDetached(fragmentManager, fragment)
                }

                override fun onFragmentResumed(fragmentManager: FragmentManager, fragment: Fragment) {
                    super.onFragmentResumed(fragmentManager, fragment)
                    registerReactionSubscriber(fragment).subscribeOn(Schedulers.newThread()).subscribe()
                }

                override fun onFragmentPaused(fragmentManager: FragmentManager, fragment: Fragment) {
                    mReactionSubscribers.remove(fragment)
                    super.onFragmentPaused(fragmentManager, fragment)
                }
            }, true)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        mReactionSubscribers.remove(activity)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    fun getActionSubscriberMethods(action: FluxxAction): Observable<HashMap<String, Any>> {
        return Observable.create { observableEmitter ->
            for (parentClass in mActionSubscribers.keys) {
                for (method in mActionSubscribers[parentClass].orEmpty()) {
                    if (action.type == method.getAnnotation(Action::class.java).actionType) {
                        val map = HashMap<String, Any>()
                        map[METHOD] = method
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
            for (parentClass in mReactionSubscribers.keys) {
                for (method in mReactionSubscribers[parentClass].orEmpty()) {
                    if (reaction.type == method.getAnnotation(Reaction::class.java).reactionType) {
                        val map = HashMap<String, Any>()
                        map[METHOD] = method
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
                for (method in parentClass.javaClass.declaredMethods) {
                    val paramTypes = method.parameterTypes
                    if (method.isAnnotationPresent(Action::class.java) && paramTypes.size == 1 && paramTypes[0] == FluxxAction::class.java) {
                        classMethods.add(method)
                    }
                }
                mActionSubscribers[parentClass] = classMethods
            }
        }
    }

    fun registerActionSubscriber(storeClass: Any) {
        if (storeClass is FluxxStore) {
            methodsWithActionAnnotation(storeClass).subscribeOn(Schedulers.newThread()).subscribe()
        }
    }

    fun registerReactionSubscriber(viewClass: Any?) : Completable {
        return Completable.fromAction {
            if (!mReactionSubscribers.containsKey(viewClass)) {
                val classMethods = HashSet<Method>()
                for (method in viewClass!!.javaClass.declaredMethods) {
                    val paramTypes = method.parameterTypes
                    if (method.isAnnotationPresent(Reaction::class.java) && paramTypes.size == 1 && paramTypes[0] == FluxxReaction::class.java) {
                        classMethods.add(method)
                    }
                }
                mReactionSubscribers[viewClass] = classMethods
            }
        }
    }

    fun unregesterReactionSubscriber(view: Any) {
        if (mReactionSubscribers.containsKey(view)) {
            mReactionSubscribers.remove(view)
        }
    }

    companion object {
        var sInstance: Fluxx? = null
            private set
        const val ACTION = "action"
        const val CLASS = "class"
        const val REACTION = "reaction"
    }
}