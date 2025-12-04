package com.suvojeet.clock.data.alexa

import android.content.Context
import android.content.SharedPreferences
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest
import com.amazon.identity.auth.device.api.authorization.ProfileScope
import com.amazon.identity.auth.device.api.workflow.RequestContext

object AlexaAuthManager {
    private const val PREF_NAME = "AlexaPrefs"
    private const val KEY_TOKEN = "alexa_access_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        getPrefs(context).edit().remove(KEY_TOKEN).apply()
    }

    fun isLinked(context: Context): Boolean {
        return getToken(context) != null
    }

    fun startLogin(requestContext: RequestContext) {
        AuthorizationManager.authorize(
            AuthorizeRequest.Builder(requestContext)
                .addScopes(ProfileScope.profile())
                .build()
        )
    }

    fun logout(context: Context, listener: () -> Unit) {
        AuthorizationManager.signOut(context, object : com.amazon.identity.auth.device.api.Listener<Void, AuthError> {
            override fun onSuccess(p0: Void?) {
                clearToken(context)
                listener()
            }

            override fun onError(p0: AuthError?) {
                clearToken(context)
                listener()
            }
        })
    }
}
