package com.cst.autotest.location

import androidx.test.uiautomator.Until
import com.cst.autotest.Holder
import com.cst.autotest.ObjectStore
import com.cst.autotest.utils.bySelectorBundleFromString
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.swagger.experimental.HttpException
import io.swagger.server.models.ObjectRef
import io.swagger.server.models.Selector
import io.swagger.server.models.toBySelector
import kotlin.reflect.jvm.jvmName

private const val TAG = "Until"

/**
 * See https://github.com/ktorio/ktor/issues/1660 for the reason why we need the extra parameter
 * in nested classes:
 *
 * "One of the problematic features is nested location classes and nested location objects.
 *
 * What we are thinking of to change:
 *
 * a nested location class should always have a property of the outer class or object
 * nested objects in objects are not allowed
 * The motivation for the first point is the fact that a location class nested to another, makes no
 * sense without the ability to refer to the outer class."
 */
@KtorExperimentalLocationsAPI
@Location("/until")
class Until {
    @Location("/{oid}/dump")
    /*inner*/ class Dump(private val oid:Int, private val parent: com.cst.autotest.location.Until = com.cst.autotest.location.Until()) {
        private val holder: Holder = Holder
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
        fun response(): String {
            com.cst.autotest.location.Until.until(oid, objectStore)?.let {
                return@response it.toString() }
            throw com.cst.autotest.location.Until.notFound(oid)
        }
    }

    @Location("/findObject")
    /*inner*/ class FindObject(
        private val parent: com.cst.autotest.location.Until = com.cst.autotest.location.Until()) {

        class Get(
            private val bySelector: String,
            private val parent: FindObject = FindObject()
        ) {
            private val holder: Holder = Holder
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
            fun response(): ObjectRef {
                val bsb = bySelectorBundleFromString(bySelector)
                val searchCondition = Until.findObject(bsb.selector)
                val oid = objectStore.put(searchCondition)
                val className = searchCondition::class.jvmName
                return ObjectRef(oid, className)
            }
        }

        class Post(
            private val selector: Selector? = null,
            private val parent: FindObject = FindObject()
        ) {
            private val holder: Holder = Holder
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
            fun response(selector: Selector): ObjectRef {
                val searchCondition = Until.findObject(selector.toBySelector())
                val oid = objectStore.put(searchCondition)
                val className = searchCondition::class.jvmName
                return ObjectRef(oid, className)
            }
        }
    }

    @Location("/findObjects")
    /*inner*/ class FindObjects(
        private val parent: com.cst.autotest.location.Until = com.cst.autotest.location.Until()) {

        class Get(
            private val bySelector: String,
            private val parent: FindObjects = FindObjects()
        ) {
            private val holder: Holder = Holder
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
            fun response(): ObjectRef {
                val bsb = bySelectorBundleFromString(bySelector)
                val searchCondition = Until.findObjects(bsb.selector)
                val oid = objectStore.put(searchCondition)
                val className = searchCondition::class.jvmName
                return ObjectRef(oid, className)
            }
        }

        class Post(
            private val selector: Selector? = null,
            private val parent: FindObjects = FindObjects()
        ) {
            private val holder: Holder = Holder
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
            fun response(selector: Selector): ObjectRef {
                val searchCondition = Until.findObjects(selector.toBySelector())
                val oid = objectStore.put(searchCondition)
                val className = searchCondition::class.jvmName
                return ObjectRef(oid, className)
            }
        }
    }

    @Location("/newWindow")
    /*inner*/ class NewWindow(private val parent: com.cst.autotest.location.Until = com.cst.autotest.location.Until()) {
        private val holder: Holder = Holder
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
        fun response(): ObjectRef {
            val eventCondition = Until.newWindow()
            val oid = objectStore.put(eventCondition)
            return ObjectRef(oid, eventCondition::class.jvmName)
        }
    }

    companion object {
        /**
         * Gets an until by its [oid].
         */
        fun until(oid: Int, objectStore: ObjectStore) =
            objectStore[oid] as androidx.test.uiautomator.SearchCondition<*>?

        fun notFound(oid: Int): HttpException {
            return HttpException(HttpStatusCode.NotFound, "⚠️ Until with oid=${oid} not found")
        }
    }
}