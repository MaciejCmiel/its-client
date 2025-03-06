/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 */

use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;
use crate::transport::payload::Payload;

use crossbeam_channel::Sender;
use log::{debug, error, info, trace, warn};
use rumqttc::v5::mqttbytes::QoS;
use rumqttc::v5::mqttbytes::v5::Filter;
use rumqttc::v5::{AsyncClient, Event, EventLoop, MqttOptions};

#[cfg(feature = "telemetry")]
use {
    crate::transport::telemetry::get_mqtt_span,
    opentelemetry::Context,
    opentelemetry::propagation::TextMapPropagator,
    opentelemetry::trace::{SpanKind, TraceContextExt},
    opentelemetry_sdk::propagation::TraceContextPropagator,
};

pub struct MqttClient {
    client: AsyncClient,
}

impl MqttClient {
    pub fn new(options: &MqttOptions) -> (Self, EventLoop) {
        let (client, event_loop) = AsyncClient::new(options.clone(), 1000);
        (MqttClient { client }, event_loop)
    }

    pub async fn subscribe(&mut self, topic_list: &[String]) {
        match self
            .client
            .subscribe_many(
                topic_list
                    .iter()
                    .map(|topic| Filter::new(topic.clone(), QoS::AtMostOnce))
                    .collect::<Vec<Filter>>(),
            )
            .await
        {
            Ok(()) => debug!("sent subscriptions"),
            Err(e) => error!(
                "failed to send subscriptions, is the connection close? \nError: {:?}",
                e
            ),
        };
    }

    #[cfg(feature = "telemetry")]
    pub async fn publish<T: Topic, P: Payload>(&self, mut packet: Packet<T, P>) {
        debug!("Publish with context");
        let payload = serde_json::to_string(&packet.payload).unwrap();

        let span = get_mqtt_span(
            SpanKind::Producer,
            &packet.topic.to_string(),
            payload.len() as i64,
        );

        let cx = Context::current().with_span(span);
        let _guard = cx.attach();

        let propagator = TraceContextPropagator::new();
        propagator.inject(&mut packet);

        self.do_publish(packet).await
    }

    #[cfg(not(feature = "telemetry"))]
    pub async fn publish<T: Topic, P: Payload>(&self, packet: Packet<T, P>) {
        debug!("Publish without context");
        self.do_publish(packet).await
    }

    async fn do_publish<T: Topic, P: Payload>(&self, packet: Packet<T, P>) {
        let payload = serde_json::to_string(&packet.payload).unwrap();

        match self
            .client
            .publish_with_properties(
                packet.topic.to_string(),
                QoS::ExactlyOnce,
                false,
                payload,
                packet.properties,
            )
            .await
        {
            Ok(()) => {
                trace!("sent publish");
            }
            Err(e) => error!(
                "Failed to send publish, is the connection close? \nError: {:?}",
                e
            ),
        }
    }
}

pub async fn listen(mut event_loop: EventLoop, sender: Sender<Event>) {
    info!("listening started");
    let mut listening = true;
    while listening {
        match event_loop.poll().await {
            Ok(event) => match sender.send(event) {
                Ok(()) => trace!("item sent"),
                Err(error) => {
                    error!("stopped to send item: {}", error);
                    listening = false;
                }
            },
            Err(error) => {
                error!("stopped to receive event: {:?}", error);
                listening = false;
            }
        }
    }
    warn!("listening done");
}
