// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.events;

public abstract class EventOnce
{
    private long invokeAt;
    private long originalMilliSecondDelay;
    private Object[] args;
    
    public EventOnce(final long invokeAt, final Object[] args) {
        this.invokeAt = invokeAt;
        this.args = args;
        this.originalMilliSecondDelay = invokeAt - System.currentTimeMillis();
    }
    
    public EventOnce(final int fromNow, final Unit unit) {
        this(fromNow, unit, new Object[0]);
    }
    
    public EventOnce(final int fromNow, final Unit unit, final Object[] args) {
        this.args = args;
        switch (unit) {
            case HOURS: {
                this.invokeAt = System.currentTimeMillis() + fromNow * 60 * 60 * 1000;
                break;
            }
            case MINUTES: {
                this.invokeAt = System.currentTimeMillis() + fromNow * 60 * 1000;
                break;
            }
            case SECONDS: {
                this.invokeAt = System.currentTimeMillis() + fromNow * 1000;
                break;
            }
            case MILLISECONDS: {
                this.invokeAt = System.currentTimeMillis() + fromNow;
                break;
            }
        }
        this.originalMilliSecondDelay = fromNow;
    }
    
    public boolean isInvokable() {
        return this.invokeAt > 0L && System.currentTimeMillis() > this.invokeAt;
    }
    
    public abstract boolean invoke();
    
    public long getInvokeAt() {
        return this.invokeAt;
    }
    
    public void setInvokeAt(final long invokeAt) {
        this.invokeAt = invokeAt;
    }
    
    public Object[] getArgs() {
        return this.args;
    }
    
    public void setArgs(final Object[] args) {
        this.args = args;
    }
    
    public long getOriginalMilliSecondDelay() {
        return this.originalMilliSecondDelay;
    }
    
    public enum Unit
    {
        MILLISECONDS("MILLISECONDS", 0), 
        SECONDS("SECONDS", 1), 
        MINUTES("MINUTES", 2), 
        HOURS("HOURS", 3);
        
        Unit(final String s, final int n) {
        }
    }
}
