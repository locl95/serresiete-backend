package com.kos.eventsourcing.subscriptions

import com.kos.common.WithLogger
import com.kos.eventsourcing.subscriptions.repository.SubscriptionsRepository

class EventSubscriptionService(
    subscriptionsRepository: SubscriptionsRepository
) : WithLogger("eventSubscriptionService") {
    suspend fun getQueueStatus(): List<Pair<String, SubscriptionState>> {
        return listOf()
    }
}