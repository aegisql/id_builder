package com.aegisql.id_builder.impl;

/**
 * The type Id state.
 * Keeps immutable ID state
 */
record IdState(long globalCounter, long currentId, long currentTimeStampSec) {
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        IdState idState = (IdState) o;
        return globalCounter == idState.globalCounter;
    }
}
