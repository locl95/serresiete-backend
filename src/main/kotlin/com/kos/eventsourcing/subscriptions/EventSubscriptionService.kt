package com.kos.eventsourcing.subscriptions

import com.kos.common.WithLogger
import com.kos.eventsourcing.subscriptions.repository.SubscriptionsRepository

class EventSubscriptionService(
    private val subscriptionsRepository: SubscriptionsRepository
) : WithLogger("eventSubscriptionService") {
    suspend fun getEventSubscritpions(): Map<String, SubscriptionState> = subscriptionsRepository.state()
}