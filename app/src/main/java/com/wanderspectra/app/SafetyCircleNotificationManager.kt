package com.wanderspectra.app

import android.content.Context

object SafetyCircleNotificationManager {

    data class NotificationResult(
        val sentCount: Int,
        val failedCount: Int,
        val skippedCount: Int,
        val errors: List<String>
    )

    fun notifySafetyCircle(
        context: Context,
        caregiverId: String,
        childId: String,
        message: String,
        onComplete: (NotificationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (caregiverId.isBlank() || childId.isBlank()) {
            onError("Caregiver or child information is missing.")
            return
        }

        if (message.isBlank()) {
            onError("The notification message cannot be empty.")
            return
        }

        if (!SmsHelper.hasSmsPermission(context)) {
            onError("SMS permission is required to notify your Safety Circle.")
            return
        }

        val repository = SafetyCircleRepository()

        repository.getMembersForCaregiver(
            caregiverId = caregiverId,
            onSuccess = { members ->

                val eligibleMembers = members.filter {
                    it.isActive &&
                            it.elopementAlertsEnabled &&
                            it.phone.isNotBlank()
                }

                var skippedCount = members.size - eligibleMembers.size

                if (eligibleMembers.isEmpty()) {
                    onComplete(
                        NotificationResult(
                            sentCount = 0,
                            failedCount = 0,
                            skippedCount = skippedCount,
                            errors = emptyList()
                        )
                    )
                    return@getMembersForCaregiver
                }

                var remainingChecks = eligibleMembers.size
                val assignedMembers = mutableListOf<SafetyCircleMember>()
                val errors = mutableListOf<String>()

                eligibleMembers.forEach { member ->
                    repository.isMemberAssignedToChild(
                        member = member,
                        childId = childId,
                        onSuccess = { assigned ->
                            if (assigned) {
                                assignedMembers.add(member)
                            } else {
                                skippedCount++
                            }

                            remainingChecks--

                            if (remainingChecks == 0) {
                                sendNotifications(
                                    context = context,
                                    members = assignedMembers,
                                    message = message,
                                    skippedCount = skippedCount,
                                    initialErrors = errors,
                                    onComplete = onComplete
                                )
                            }
                        },
                        onError = { exception ->
                            errors.add(
                                "${member.name}: Unable to verify child assignment. " +
                                        (exception.localizedMessage ?: "")
                            )

                            skippedCount++
                            remainingChecks--

                            if (remainingChecks == 0) {
                                sendNotifications(
                                    context = context,
                                    members = assignedMembers,
                                    message = message,
                                    skippedCount = skippedCount,
                                    initialErrors = errors,
                                    onComplete = onComplete
                                )
                            }
                        }
                    )
                }
            },
            onError = { exception ->
                onError(
                    exception.localizedMessage
                        ?: "Unable to load Safety Circle members."
                )
            }
        )
    }

    private fun sendNotifications(
        context: Context,
        members: List<SafetyCircleMember>,
        message: String,
        skippedCount: Int,
        initialErrors: List<String>,
        onComplete: (NotificationResult) -> Unit
    ) {
        if (members.isEmpty()) {
            onComplete(
                NotificationResult(
                    sentCount = 0,
                    failedCount = 0,
                    skippedCount = skippedCount,
                    errors = initialErrors.toList()
                )
            )
            return
        }

        var sentCount = 0
        var failedCount = 0
        var remainingMessages = members.size
        val errors = initialErrors.toMutableList()

        members.forEach { member ->
            SmsHelper.sendSms(
                context = context,
                phoneNumber = member.phone,
                message = message,
                onSuccess = {
                    sentCount++
                    remainingMessages--

                    if (remainingMessages == 0) {
                        onComplete(
                            NotificationResult(
                                sentCount = sentCount,
                                failedCount = failedCount,
                                skippedCount = skippedCount,
                                errors = errors.toList()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    failedCount++
                    errors.add("${member.name}: $error")
                    remainingMessages--

                    if (remainingMessages == 0) {
                        onComplete(
                            NotificationResult(
                                sentCount = sentCount,
                                failedCount = failedCount,
                                skippedCount = skippedCount,
                                errors = errors.toList()
                            )
                        )
                    }
                }
            )
        }
    }
}


