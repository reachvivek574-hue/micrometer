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

import io.micrometer.core.instrument.AbstractMeter;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.distribution.TimeWindowMax;
import io.micrometer.core.instrument.util.MeterEquivalence;

/**
 * A gauge that tracks the maximum value recorded within a sliding time window.
 * Unlike a regular {@link Gauge} which captures an instantaneous sample, this
 * meter retains the peak value seen within the configured window, making it
 * suitable for tracking metrics like maximum active connections where spikes
 * between scrape intervals would otherwise be missed.
 *
 * @author Jon Schneider
 */
public class TimeWindowMaxGauge extends AbstractMeter implements Gauge {

    private final TimeWindowMax timeWindowMax;

    public TimeWindowMaxGauge(Meter.Id id, Clock clock, long rotateFrequencyMillis, int bufferLength) {
        super(id);
        this.timeWindowMax = new TimeWindowMax(clock, rotateFrequencyMillis, bufferLength);
    }

    /**
     * Record a value to be considered for the time-windowed maximum.
     *
     * @param amount The value to record.
     */
    public void record(double amount) {
        timeWindowMax.record(amount);
    }

    @Override
    public double value() {
        return timeWindowMax.poll();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return MeterEquivalence.equals(this, o);
    }

    @Override
    public int hashCode() {
        return MeterEquivalence.hashCode(this);
    }
}
