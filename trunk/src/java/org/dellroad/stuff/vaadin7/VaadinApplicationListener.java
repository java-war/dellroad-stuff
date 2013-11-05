
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin7;

import com.vaadin.server.VaadinSession;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SmartApplicationListener;

/**
 * A Spring {@link org.springframework.context.ApplicationListener} support superclass customized for use by
 * listeners that are part of a Vaadin application when listening to non-Vaadin application event sources.
 *
 * <p>
 * Listeners that are part of a Vaadin application should use this superclass if they are going to be registered
 * with non-Vaadin event multicasters. This will ensure that events are delivered
 * in the proper Vaadin application context and memory leaks are avoided.
 * </p>
 *
 * @param <E> The type of the event
 * @see VaadinExternalListener
 * @see VaadinUtil#invoke
 * @see VaadinApplicationScope
 * @see SpringVaadinSessionListener
 */
public abstract class VaadinApplicationListener<E extends ApplicationEvent>
  extends VaadinExternalListener<ApplicationEventMulticaster> implements SmartApplicationListener {

    private final Class<E> eventType;

    /**
     * Convenience constructor. Equivalent to:
     * <blockquote>
     *  {@link #VaadinApplicationListener(ApplicationEventMulticaster, Class, VaadinSession)
     *      VaadinApplicationListener(multicaster, eventType, VaadinUtil.getCurrentSession())}
     * </blockquote>
     *
     * @param multicaster the application event multicaster on which this listener will register
     * @param eventType type of events this instance should receive (others will be ignored)
     * @throws IllegalArgumentException if {@code eventType} is null
     * @throws IllegalArgumentException if there is no {@link VaadinSession} associated with the current thread
     */
    public VaadinApplicationListener(ApplicationEventMulticaster multicaster, Class<E> eventType) {
        this(multicaster, eventType, VaadinUtil.getCurrentSession());
    }

    /**
     * Primary constructor.
     *
     * @param multicaster the application event multicaster on which this listener will register
     * @param eventType type of events this instance should receive (others will be ignored)
     * @param session the associated Vaadin application
     * @throws IllegalArgumentException if either parameter is null
     */
    public VaadinApplicationListener(ApplicationEventMulticaster multicaster, Class<E> eventType, VaadinSession session) {
        super(multicaster, session);
        if (eventType == null)
            throw new IllegalArgumentException("null eventType");
        this.eventType = eventType;
    }

    /**
     * Get the event type that this listener listens for.
     */
    public final Class<E> getEventType() {
        return this.eventType;
    }

    @Override
    protected void register(ApplicationEventMulticaster eventSource) {
        this.getEventSource().addApplicationListener(this);
    }

    @Override
    protected void unregister(ApplicationEventMulticaster eventSource) {
        this.getEventSource().removeApplicationListener(this);
    }

    @Override
    public final void onApplicationEvent(ApplicationEvent event) {
        E castEvent;
        try {
            castEvent = this.eventType.cast(event);
        } catch (ClassCastException e) {
            // should not happen
            return;
        }
        final E castEvent2 = castEvent;
        this.handleEvent(new Runnable() {
            @Override
            public void run() {
                VaadinApplicationListener.this.onApplicationEventInternal(castEvent2);
            }
        });
    }

    /**
     * Handle a listener event. When this method is invoked, it will already be within the context
     * of the {@link VaadinSession} with which this listener is associated.
     *
     * @see VaadinUtil#invoke
     * @see VaadinUtil#getCurrentSession
     */
    protected abstract void onApplicationEventInternal(E event);

    /**
     * Determine whether this listener actually supports the given event type.
     *
     * <p>
     * The implementation in {@link VaadinApplicationListener} tests whether {@code eventType}
     * is assignable to the type given in the constructor. Subclasses may override as desired.
     */
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return this.eventType.isAssignableFrom(eventType);
    }

    /**
     * Determine whether this listener actually supports the given source type.
     *
     * <p>
     * The implementation in {@link VaadinApplicationListener} always returns true. Subclasses may override as desired.
     */
    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    /**
     * Get ordering value.
     *
     * <p>
     * The implementation in {@link VaadinApplicationListener} always returns zero. Subclasses may override as desired.
     */
    @Override
    public int getOrder() {
        return 0;
    }
}

