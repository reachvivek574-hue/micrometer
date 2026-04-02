/**
 * Copyright 2020 VMware, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.internal;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimeWindowMaxGaugeTest {

    private final MockClock clock = new MockClock();

    private TimeWindowMaxGauge createGauge() {
        Meter.Id id = new Meter.Id("test.max", Tags.empty(), null, null, Meter.Type.GAUGE);
        return new TimeWindowMaxGauge(id, clock, 40_000, 3);
    }

    @Test
    void maxTracksHighestRecordedValue() {
        TimeWindowMaxGauge gauge = createGauge();

        gauge.record(10);
        gauge.record(50);
        gauge.record(30);

        assertThat(gauge.value()).isEqualTo(50);
    }

    @Test
    void maxDecaysAfterTimeWindowExpires() {
        TimeWindowMaxGauge gauge = createGauge();

        gauge.record(100);
        assertThat(gauge.value()).isEqualTo(100);

        // Advance past the full window (3 buckets * 40s = 120s)
        clock.add(121, TimeUnit.SECONDS);

        gauge.record(5);
        assertThat(gauge.value()).isEqualTo(5);
    }

    @Test
    void measureReturnsSingleValueStatistic() {
        TimeWindowMaxGauge gauge = createGauge();
        gauge.record(42);

        assertThat(gauge.measure()).hasSize(1);
        assertThat(gauge.measure().iterator().next().getStatistic()).isEqualTo(Statistic.VALUE);
        assertThat(gauge.measure().iterator().next().getValue()).isEqualTo(42);
    }

    @Test
    void valueIsZeroWithNoRecordings() {
        TimeWindowMaxGauge gauge = createGauge();
        assertThat(gauge.value()).isEqualTo(0);
    }
}
