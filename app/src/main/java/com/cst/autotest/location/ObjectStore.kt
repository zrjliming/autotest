package com.cst.autotest.location

import com.cst.autotest.Holder

import androidx.test.uiautomator.StaleObjectException
import io.ktor.locations.*
import io.swagger.server.models.StatusResponse

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
@Location("/objectStore")
class ObjectStore {
    @Location("/clear")
    /*inner*/ class Clear(private val parent: com.cst.autotest.location.ObjectStore = com.cst.autotest.location.ObjectStore()) {
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
        fun response(): StatusResponse {
            objectStore.clear()
            return StatusResponse(StatusResponse.Status.OK)
        }
    }

    @Location("/list")
    /*inner*/ class List(private val parent: com.cst.autotest.location.ObjectStore = com.cst.autotest.location.ObjectStore()) {
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
        // FIXME: should be more specific than Any
        fun response(): Any {
            val a = ArrayList<Any>()
            objectStore.list().forEach { (k, v) ->
                try {
                    a.add(OidObj(k, v.toString()))
                } catch (e: StaleObjectException) {
                    objectStore.remove(k)
                }
            }
            return a
        }
    }

    @Location("/remove")
    /*inner*/ class Remove(
        val oid: Int,
        private val parent: com.cst.autotest.location.ObjectStore = com.cst.autotest.location.ObjectStore()
    ) {
        private val objectStore: com.cst.autotest.ObjectStore = Holder.objectStore
        fun response(): Any {
            objectStore.remove(oid)
            return StatusResponse(StatusResponse.Status.OK)
        }
    }
}

// TODO: perhaps we should unify with ObjectRef
data class OidObj(val oid: Int, val obj: Any)
