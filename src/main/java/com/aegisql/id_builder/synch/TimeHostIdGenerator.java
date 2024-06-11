package com.aegisql.id_builder.synch;

import com.aegisql.id_builder.IdSourceException;

import java.util.function.LongSupplier;

import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.TimeTransformer;

import static com.aegisql.id_builder.TimeTransformer.identity;

/**
 * The Class TimeHostIdGenerator.
 */
@Deprecated(since = "1.1; Replaced by DecimalIdGenerator")
public class TimeHostIdGenerator implements IdSource {

    /**
     * The max id.
     */
    protected final int maxId;

    /**
     * The max host id.
     */
    protected final int maxHostId;

    /**
     * The host id.
     */
    protected final long hostId;

    /**
     * The time id base.
     */
    protected final long timeIdBase;

    /**
     * The max id per M sec.
     */
    protected final long maxIdPerMSec;

    /**
     * The global counter.
     */
    protected volatile long globalCounter       = 0;

    /**
     * The current id.
     */
    protected long currentId           = 0;

    /**
     * The current time stamp sec.
     */
    protected long currentTimeStampSec;

    /** The sleep after. */
    private long sleepAfter;

    /** The tf. */
    private TimeTransformer tf;

    /** The timestamp. */
    private LongSupplier timestamp = System::currentTimeMillis;

    /**
     * Instantiates a new time host id generator.
     *
     * @param hostId            the host id
     * @param startTimeStampSec the start time stamp sec
     * @param maxId             the max id
     * @param maxHostId         the max host id
     */
    public TimeHostIdGenerator(int hostId, long startTimeStampSec, int maxId, int maxHostId) {
        this.maxId               = maxId;
        this.maxHostId           = maxHostId;
        this.currentTimeStampSec = startTimeStampSec;

        if (hostId > maxHostId) {
            throw new IdSourceException("Max host ID > " + maxHostId);
        }

        this.hostId       = (long) hostId * (maxId + 1);
        this.timeIdBase   = (long) (maxHostId + 1) * (maxId + 1);
        this.maxIdPerMSec = (maxId + 1) / 1000;
        this.setPastShiftSlowDown(1.2);
    }

    /**
     * Sets the past shift slow down.
     *
     * @param x the new past shift slow down
     */
    public void setPastShiftSlowDown(double x) {
        this.sleepAfter = Math.round((double) (maxId + 1) / 1000 / x);
    }

    /* (non-Javadoc)
     * @see com.aegisql.id_builder.IdSource#getId()
     */
    @Override
    public final synchronized long getId() {
        long nowMs = timestamp.getAsLong();
        long now   = nowMs / 1000;
        long dt    = nowMs - (now * 1000);


        if (now > currentTimeStampSec)
            initNextSecondId(now);
        else if (now == currentTimeStampSec)
            currentSecondNextId(dt);
        else
            processShiftToPastTime();

        globalCounter++;
        return buildId(currentId++);
    }

    private long buildId(long id) {
        if (id > maxId) {
            throw new IdSourceException("Internal ID reached ultimate value: " + id);
        }
        return tf.transformTimestamp(currentTimeStampSec) * timeIdBase + hostId + id;
    }

    /**
     * Sleep one M sec.
     */
    private static void sleepOneMSec() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new IdSourceException("Unexpected Interruption", e);
        }
    }

    /**
     * Current second next id.
     *
     * @param dt the dt
     */
    private void currentSecondNextId(long dt) {
        long maxPredictedId = Math.min(maxId, dt * maxIdPerMSec);
        if (currentId >= maxPredictedId) {
            sleepOneMSec();
            long nowMs = timestamp.getAsLong();
            long now = nowMs / 1000;
            if (now > currentTimeStampSec) {
                initNextSecondId(now);
            }
        }
    }

    /**
     * Inits the next second id.
     *
     * @param now the now ms
     */
    private void initNextSecondId(long now) {
        currentId = 0;
        currentTimeStampSec = now;
    }

    /**
     * Process shift to past time.
     *
     */
    private void processShiftToPastTime() {
        if (globalCounter % sleepAfter == 0) {
            sleepOneMSec();
        }
        if (currentId >= maxId) {
            currentId = 0;
            currentTimeStampSec++; // add one second to current timestamp
        }
    }

    /**
     * Sets the time transformer.
     *
     * @param tf the new time transformer
     */
    public void setTimeTransformer(TimeTransformer tf) {
        this.tf = tf;
    }

    /**
     * Id generator 10 x 4 x 5.
     *
     * @param hostId            the host id
     * @param startTimeStampSec the start time stamp sec
     * @return the time host id generator
     */
    public static TimeHostIdGenerator idGenerator_10x4x5(int hostId, long startTimeStampSec) {
        TimeHostIdGenerator idGen = new TimeHostIdGenerator(hostId, startTimeStampSec, 99999, 9999);
        idGen.setTimeTransformer(identity);
        return idGen;
    }

    /**
     * Id generator 10 x 4 x 5.
     *
     * @param hostId the host id
     * @return the time host id generator
     */
    public static TimeHostIdGenerator idGenerator_10x4x5(int hostId) {
        return idGenerator_10x4x5(hostId, System.currentTimeMillis() / 1000);
    }

    /**
     * Id generator 10 x 8.
     *
     * @param startTimeStampSec the start time stamp sec
     * @return the time host id generator
     */
    public static TimeHostIdGenerator idGenerator_10x8(long startTimeStampSec) {
        TimeHostIdGenerator idGen = new TimeHostIdGenerator(0, startTimeStampSec, 99999999, 9);
        idGen.setTimeTransformer(identity);
        return idGen;
    }

    /**
     * Id generator 10 x 8.
     *
     * @return the time host id generator
     */
    public static TimeHostIdGenerator idGenerator_10x8() {
        TimeHostIdGenerator idGen = new TimeHostIdGenerator(0, System.currentTimeMillis()/1000, 99999999, 9);
        idGen.setTimeTransformer(identity);
        return idGen;
    }


    /**
     * Sets the timestamp supplier.
     *
     * @param timestamp the new timestamp supplier
     */
    public void setTimestampSupplier(LongSupplier timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the global counter.
     *
     * @return the global counter
     */
    public long getGlobalCounter() {
        return globalCounter;
    }

}
