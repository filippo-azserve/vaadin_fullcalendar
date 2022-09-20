/*
 * Copyright 2020, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import lombok.Getter;

import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.LazyInMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.model.Footer;
import org.vaadin.stefan.fullcalendar.model.Header;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@NpmPackage(value = "@fullcalendar/core", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/interaction", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/daygrid", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/timegrid", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/list", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "moment", version = "2.29.1")
@NpmPackage(value = "moment-timezone", version = "0.5.32")
@NpmPackage(value = "@fullcalendar/moment", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/moment-timezone", version = FullCalendar.FC_CLIENT_VERSION)
@Tag("full-calendar")
@JsModule("./full-calendar.js")
public class FullCalendar extends Component implements HasStyle, HasSize {

    /**
     * The library base version used in this addon. Some additional libraries might have a different version number due to
     * a different release cycle or known issues.
     */
    public static final String FC_CLIENT_VERSION = "5.10.1";

    /**
     * This is the default duration of an timeslot event in hours. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_TIMED_EVENT_DURATION = 1;

    /**
     * This is the default duration of an daily event in days. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_DAY_EVENT_DURATION = 1;

    private static final String JSON_INITIAL_OPTIONS = "initialOptions";

    /**
     * Caches the last fetched entries for entry based events.
     */
    private final Map<String, Entry> lastFetchedEntries = new HashMap<>();
    private final Map<String, Serializable> options = new HashMap<>();
    private final Map<String, Object> serverSideOptions = new HashMap<>();

    private EntryProvider<? extends Entry> entryProvider;
    private final List<Registration> entryProviderDataListeners = new LinkedList<>();

    // used to keep the amount of timeslot selected listeners. when 0, then selectable option is auto removed
    private int timeslotsSelectedListenerCount;

    private Timezone browserTimezone;

    private boolean firstAttach = true;

    @Getter
    private boolean attached;

    private String latestKnownViewName;
    private LocalDate latestKnownIntervalStart;

    private boolean refreshAllRequested;

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefault()}).
     * <p></p>
     * Uses {@link EagerInMemoryEntryProvider} by default.
     */
    public FullCalendar() {
        this(-1);
    }

    /**
     * Creates a new instance.
     * <br><br>
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views. This value has to be set here and cannot be modified afterwards due to
     * technical reasons of FC. If set afterwards the entry limit would overwrite settings
     * and would show the limit also for basic views where it makes no sense (might change in future).
     * Passing a negative number disabled the entry limit (same as passing no number at all).
     * <br><br>
     * Sets the locale to {@link CalendarLocale#getDefault()}
     * <p></p>
     * Uses {@link EagerInMemoryEntryProvider} by default.
     *
     * @param entryLimit The max number of stacked event levels within a given day. This includes the +more link if present. The rest will show up in a popover.
     */
    public FullCalendar(int entryLimit) {
        setLocale(CalendarLocale.getDefault());
        if (entryLimit >= 0) {
            getElement().setProperty("dayMaxEvents", entryLimit);
        } else {
            getElement().setProperty("dayMaxEvents", false);
        }

        postConstruct();
    }

    /**
     * Creates a new instance with custom initial options. This allows a full override of the default
     * initial options, that the calendar would normally receive. Theoretically you can set all options,
     * as long as they are not based on a client side variable (as for instance "plugins" or "locales").
     * Complex objects are possible, too, for instance for view-specific settings.
     * Please refer to the official FC documentation regarding potential options.
     * <br><br>
     * Client side event handlers, that are technically also a part of the options are still applied to
     * the options object. However you may set your own event handlers with the correct name. In that case
     * they will be taken into account instead of the default ones.
     * <br><br>
     * Plugins (key "plugins") will always be set on the client side (and thus override any key passed with this
     * object), since they are needed for a functional calendar. This may change in future. Same for locales
     * (key "locales").
     * <br><br>
     * Please be aware, that incorrect options or event handler overriding can lead to unpredictable errors,
     * which will NOT be supported in any case.
     * <br><br>
     * Also, options set this way are not cached in the server side state. Calling any of the
     * {@code getOption(...)} methods will result in {@code null} (or the respective native default).
     * <p></p>
     * Uses {@link EagerInMemoryEntryProvider} by default.
     *
     * @param initialOptions initial options
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     */
    public FullCalendar(@NotNull JsonObject initialOptions) {
        getElement().setPropertyJson(JSON_INITIAL_OPTIONS, Objects.requireNonNull(initialOptions));

        postConstruct();
    }

    /**
     * Called after the constructor has been initialized.
     */
    private void postConstruct() {
        entryProvider = new EagerInMemoryEntryProvider<>();
        entryProvider.setCalendar(this);

        addDatesRenderedListener(event -> {
            latestKnownViewName = event.getName();
            latestKnownIntervalStart = event.getIntervalStart();
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        attached = true;
        super.onAttach(attachEvent);
        if (firstAttach) {
            firstAttach = false;
        } else {
            getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(this, executionContext -> {
                    // options
                    Serializable initialOptions = getElement().getPropertyRaw(JSON_INITIAL_OPTIONS);
                    JsonObject optionsJson = Json.createObject();
                    if (initialOptions instanceof JsonObject) {
                        JsonObject initialOptionsJson = (JsonObject) initialOptions;
                        for (String key : initialOptionsJson.keys()) {
                            optionsJson.put(key, (JsonValue) initialOptionsJson.get(key));
                        }
                    }

                    if (!options.isEmpty()) {
                        options.forEach((key, value) -> optionsJson.put(key, JsonUtils.toJsonValue(value)));
                    }

                    // entries
                    boolean eagerLoadingEntryProvider = isEagerInMemoryEntryProvider();


                    // We do not use setProperty since that would also store the jsonified state in this instance.
                    // Especially with a huge amount of entries this could lead to memory issues.
                    getElement().callJsFunction("_restoreStateFromServer",
                            optionsJson,
                            !eagerLoadingEntryProvider,
                            JsonUtils.toJsonValue(latestKnownViewName),
                            JsonUtils.toJsonValue(latestKnownIntervalStart));

                });
            });
        }

        if (isEagerInMemoryEntryProvider()) {
            entryProvider.refreshAll(); // if lazy, the client side will automatically take care of refreshing
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        attached = false;
        super.onDetach(detachEvent);
    }

    /**
     * Sets a property to allow or disallow (re-)rendering of dates, when an option changes. When allowed,
     * each option will fire a dates rendering event, which can lead to multiple rendering events, even if only
     * one is needed.
     *
     * @param allow allow
     */
    public void allowDatesRenderEventOnOptionChange(boolean allow) {
        getElement().setProperty("noDatesRenderEventOnOptionSetting", !allow);
    }

    /**
     * Moves to the next interval (e. g. next month if current view is monthly based).
     */
    public void next() {
        getElement().callJsFunction("next");
    }

    /**
     * Moves to the previous interval (e. g. previous month if current view is monthly based).
     */
    public void previous() {
        getElement().callJsFunction("previous");
    }

    /**
     * Moves to the current interval (e. g. current month if current view is monthly based).
     */
    public void today() {
        getElement().callJsFunction("today");
    }

    /**
     * Sets the entry provider for this instance. The previous entry provider will be removed and the
     * client side will be updated.
     * <p/>
     * By default a new full calendar is initialized with an {@link EagerInMemoryEntryProvider}.
     *
     * @param entryProvider entry provider
     */
    public void setEntryProvider(@NotNull EntryProvider<? extends Entry> entryProvider) {
        Objects.requireNonNull(entryProvider);

        if (this.entryProvider != entryProvider) {
            if (this.entryProvider != null) {
                this.entryProvider.setCalendar(null);
            }

            this.entryProvider = entryProvider;
            entryProvider.setCalendar(this);

            entryProviderDataListeners.forEach(Registration::remove);
            entryProviderDataListeners.clear();

            entryProviderDataListeners.add(entryProvider.addEntryRefreshListener(event -> requestRefresh(event.getItemToRefresh())));

            boolean eagerLoadingInMemory = isEagerInMemoryEntryProvider();
            // refresh all is handled manually by the eager memory provider, therefore we have to check this
            // might change in future
            if (eagerLoadingInMemory) {
                entryProvider.refreshAll(); // triggers to push all data to the client
            } else {

                entryProviderDataListeners.add(entryProvider.addEntriesChangeListener(event -> requestRefreshAll()));

            }

            getElement().callJsFunction("setHasLazyLoadingEntryProvider", !eagerLoadingInMemory);
        }
    }

    /**
     * Returns the entry provider of this calendar. Never null.
     * @param <T> entry provider class or subclass
     * @return entry provider
     */
    @SuppressWarnings("unchecked")
    public <T extends EntryProvider<? extends Entry>> T getEntryProvider() {
        return (T) entryProvider;
    }

    /**
     * This method requests an entry refresh from the client side. Every call of this method will register
     * a client side call, since it might be called for different items. Calls are handled in the order
     * they are requested. This method will not interfere or "communicate" with {@link #requestRefreshAll()}.
     * <p></p>
     * <i>The native client itself does not support fetching single items. Instead we convert the item and
     * push it directly to the client, where it will be handled.</i>
     * @param item item to refresh
     */
    protected void requestRefresh(@NotNull Entry item) {
        getElement().getNode().runWhenAttached(ui -> {
            ui.beforeClientResponse(this, pExecutionContext -> {
                getEntryProvider()
                        .fetchById(item.getId())
                        .ifPresent(refreshedEntry -> {
                            lastFetchedEntries.put(refreshedEntry.getId(), refreshedEntry);
                            getElement().callJsFunction("refreshSingleEvent", refreshedEntry.toJson());
                        });
                refreshAllRequested = false;
            });
        });
    }

    /**
     * Informs the client side, that a "refresh all" has been requested. Subsequent calls to this method during the
     * same request cycle will still just result in one fetch from the client side (in fact, only one call to the
     * client will be executed). This behavior might change in future, if it appears to be problematic regarding
     * other client side calls.
     * <p></p>
     * When parallel to this call {@link #requestRefresh(Entry)} is called, the calls will be handled in the order
     * they are called, whereas only the first call of this method is relevant.
     */
    protected void requestRefreshAll() {
        if (!refreshAllRequested) {
            refreshAllRequested = true;
            getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(this, pExecutionContext -> {
                    getElement().callJsFunction("refreshAllEvents");
                    refreshAllRequested = false;
                });
            });
        }
    }

    /**
     * Indicates, if the entry provider is eager loading, i.e. takes care of pushing entries manually as
     * json array, or not, i.e. the client side takes care of fetching data from the server.
     *
     * @return is eager loading
     */
    public boolean isEagerInMemoryEntryProvider() {
        return entryProvider instanceof EagerInMemoryEntryProvider;
    }

    /**
     * Indicates, if the entry provider is a in memory provider.
     *
     * @return is eager loading
     */
    public boolean isInMemoryEntryProvider() {
        return entryProvider instanceof InMemoryEntryProvider;
    }

    @ClientCallable
    protected JsonArray fetchFromServer(@NotNull JsonObject query) {
        Objects.requireNonNull(query);
        Objects.requireNonNull(entryProvider);

        if (isEagerInMemoryEntryProvider()) {
            throw new UnsupportedOperationException("This method must not be called for eager loading entry providers");
        }

        lastFetchedEntries.clear();

        LocalDateTime start = query.hasKey("start") ? JsonUtils.parseClientSideDateTime(query.getString("start")) : null;
        LocalDateTime end = query.hasKey("end") ? JsonUtils.parseClientSideDateTime(query.getString("end")) : null;

        JsonArray array = Json.createArray();
        entryProvider.fetch(new EntryQuery(start, end, EntryQuery.AllDay.BOTH))
                .peek(entry -> {
                    entry.setCalendar(this);
                    entry.setKnownToTheClient(true); // mark entry as "has been sent to client"
                    lastFetchedEntries.put(entry.getId(), entry);
                })
                .map(Entry::toJson)
                .forEach(jsonObject -> array.set(array.length(), jsonObject));

        return array;
    }

    /**
     * Returns an entry with the given id from the last fetched set of entries. Returns an empty instance,
     * when there was no fetch yet or the id is unknown.
     * <p></p>
     * Uses {@link EagerInMemoryEntryProvider#getEntryById(String)} when the eager in memory provider is used.
     * <p></p>
     * This method is an internal method, intended to be used by entry based events only. Do not use it for
     * any other purpose as the implementation or scope may change in future.
     *
     * @param id id
     * @return cached entry from last fetch or empty
     */
    public Optional<Entry> getCachedEntryFromFetch(String id) {
        return isEagerInMemoryEntryProvider() ? assureEagerInMemoryProvider().getEntryById(id) : Optional.ofNullable(lastFetchedEntries.get(id));
    }

    protected InMemoryEntryProvider<Entry> assureInMemoryProvider() {
        if (!(entryProvider instanceof InMemoryEntryProvider)) {
            throw new UnsupportedOperationException("Needs an InMemoryEntryProvider to work.");
        }

        return (InMemoryEntryProvider<Entry>) entryProvider;
    }

    protected EagerInMemoryEntryProvider<Entry> assureEagerInMemoryProvider() {
        if (!(entryProvider instanceof EagerInMemoryEntryProvider)) {
            throw new UnsupportedOperationException("Needs an EagerInMemoryEntryProvider to work.");
        }

        return (EagerInMemoryEntryProvider<Entry>) entryProvider;
    }
    
    public void setEntries(Entry... entries) {
    	EagerInMemoryEntryProvider<Entry> entryProvider = EntryProvider.eagerInMemoryFromItems(entries);
    	setEntryProvider(entryProvider);
    }
    
	public void setEntries(boolean lazy, Entry... entries) {
		if(lazy) {
			LazyInMemoryEntryProvider<Entry> entryProvider = EntryProvider.lazyInMemoryFromItems(entries);
			setEntryProvider(entryProvider);
		} else {
			setEntries(entries);
		}
	}

    /**
     * Change the view of the calendar (e. g. from monthly to weekly)
     *
     * @param view view to set
     * @throws NullPointerException when null is passed
     */
    public void changeView(@NotNull CalendarView view) {
        Objects.requireNonNull(view);
        getElement().callJsFunction("changeView", view.getClientSideValue());
    }

    /**
     * Switch to the interval containing the given date (e. g. to month "October" if the "15th October ..." is passed).
     *
     * @param date date to goto
     * @throws NullPointerException when null is passed
     */
    public void gotoDate(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        getElement().callJsFunction("gotoDate", date.toString());
    }
    
    /**
     * Programatically scroll the current view to the given time in the format `hh:mm:ss.sss`, `hh:mm:sss` or `hh:mm`. For example, '05:00' signifies 5 hours.
     * 
     * @param duration duration
     * @throws NullPointerException when null is passed
     */
    public void scrollToTime(@NotNull String duration) {
        Objects.requireNonNull(duration);	// No format check, it is already done in the calendar code
        getElement().callJsFunction("scrollToTime", duration);
    }
    
    /**
     * Programatically scroll the current view to the given time in the format `hh:mm:ss.sss`, `hh:mm:sss` or `hh:mm`. For example, '05:00' signifies 5 hours.
     * 
     * @param duration duration
     * @throws NullPointerException when null is passed
     */
    public void scrollToTime(@NotNull LocalTime duration) {
        Objects.requireNonNull(duration);
        getElement().callJsFunction("scrollToTime", duration.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull Option option, Serializable value) {
        setOption(option, value, null);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(Option)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Example:
     * <pre>
     * // sends a client parseable version to client and stores original in server side
     * calendar.setOption(Option.LOCALE, locale.toLanguageTag().toLowerCase(), locale);
     *
     * // returns the original locale (as optional)
     * Optional&lt;Locale&gt; optionalLocale = calendar.getOption(Option.LOCALE)
     * </pre>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull Option option, Serializable value, Object valueForServerSide) {
        setOption(option.getOptionKey(), value, valueForServerSide);
    }

    /**
     * Sets a custom option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, JsonValue value) {
        setOption(option, value, null);
    }

    /**
     * Sets a custom option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(Option)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <p>
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, JsonValue value, Object valueForServerSide) {
        setOption(option, (Serializable) value, valueForServerSide);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, Serializable value) {
        setOption(option, value, null);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(Option)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <p>
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, Serializable value, Object valueForServerSide) {
        Objects.requireNonNull(option);

        if (value == null) {
            options.remove(option);
            serverSideOptions.remove(option);
        } else {
            options.put(option, value);

            if (valueForServerSide == null || valueForServerSide.equals(value)) {
                serverSideOptions.remove(option);
            } else {
                serverSideOptions.put(option, valueForServerSide);
            }
        }
        getElement().callJsFunction("setOption", option, value);
    }

    /**
     * Sets the first day of a week to be shown by the calendar. Per default sunday.
     * <br><br>
     * <b>Note:</b> FC works internally with 0 for sunday. This method converts SUNDAY to
     * this number before passing it to the client.
     *
     * @param firstDay first day to be shown
     * @throws NullPointerException when null is passed
     */
    public void setFirstDay(@NotNull DayOfWeek firstDay) {
        Objects.requireNonNull(firstDay);
        int value = firstDay == DayOfWeek.SUNDAY ? 0 : firstDay.getValue();
        setOption(Option.FIRST_DAY, value, firstDay);
    }

    /**
     * Sets the calendar's height to a fixed amount of pixels.
     *
     * @param heightInPixels height in pixels (e.g. 300)
     */
    public void setHeight(int heightInPixels) {
        setOption(Option.HEIGHT, heightInPixels);
    }

    /**
     * Sets the calendar's height to be calculated from parents height. Please be aware, that a block parent with
     * relative height (e. g. 100%) might not work properly. In this case use flex layout or set a fixed height for
     * the parent or the calendar.
     */
    public void setHeightByParent() {
        setOption(Option.HEIGHT, "parent");
    }

    /**
     * Sets the calendar's height to be calculated automatically. In current implementation this means by the calendars
     * width-height-ratio.
     */
    public void setHeightAuto() {
        setOption(Option.HEIGHT, "auto");
    }

    /**
     * Set if timeslots might be selected by the user. Please see also documentation of {@link #addTimeslotsSelectedListener(ComponentEventListener)}.
     *
     * @param selectable activate selectable
     */
    public void setTimeslotsSelectable(boolean selectable) {
        setOption(Option.SELECTABLE, selectable);
    }

    /**
     * Should the calendar show week numbers (when available for the current view)?
     *
     * @param weekNumbersVisible week numbers visible
     */
    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        setOption(Option.WEEK_NUMBERS, weekNumbersVisible);
    }

    /**
     * Determines the styling for week numbers in Month and DayGrid views.
     *
     * @param weekNumbersWithinDays by default to false
     */
    public void setWeekNumbersWithinDays(boolean weekNumbersWithinDays) {
        setOption(Option.WEEK_NUMBERS_WITHIN_DAYS, weekNumbersWithinDays);
    }

    /**
     * Returns the current set locale.
     *
     * @return locale
     */
    public Locale getLocale() {
        Optional<Object> option = getOption(Option.LOCALE);

        if (!option.isPresent()) {
            return CalendarLocale.getDefault();
        }

        Object value = option.get();
        return value instanceof Locale ? (Locale) value : Locale.forLanguageTag((String) value);
    }

    /**
     * Sets the locale to be used. If invoked for the first time it will load additional language scripts.
     *
     * @param locale locale
     * @throws NullPointerException when null is passed
     */
    public void setLocale(@NotNull Locale locale) {
        Objects.requireNonNull(locale);
        setOption(Option.LOCALE, locale.toLanguageTag().toLowerCase(), locale);
    }

    /**
     * If true is passed then the calendar will show a indicator for the current time, depending on the view.
     *
     * @param shown show indicator for now
     */
    public void setNowIndicatorShown(boolean shown) {
        setOption(Option.NOW_INDICATOR, shown);
    }

    /**
     * When true is passed the day / week numbers (or texts) will become clickable by the user and fire an event
     * for the clicked day / week.
     *
     * @param clickable clickable
     */
    public void setNumberClickable(boolean clickable) {
        setOption(Option.NAV_LINKS, clickable);
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the eventClassNames callback. It must be a valid JavaScript function.
     * <br><br>
     * A ClassName Input for adding classNames to the outermost event element. If supplied as a callback function, it is called every time the associated event data changes.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * <b>Example</b>
     * <pre>
     * calendar.setEventClassNamesCallback("" +
     * "function(arg) { " +
     * "  if (arg.event.getCustomProperty('isUrgent', false)) {" +
     * "    return [ 'urgent' ];" +
     * "  } else { " +
     * "    return [ 'normal' ];" +
     * "  }" +
     * "}");
     * </pre>
     *
     * @param s function to be attached
     */
    public void setEntryClassNamesCallback(String s) {
        getElement().callJsFunction("setEventClassNamesCallback", s);
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the eventContent callback. It must be a valid JavaScript function.
     * <br><br>
     * A Content Injection Input. Generated content is inserted inside the inner-most wrapper of the event element.
     * If supplied as a callback function, it is called every time the associated event data changes.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * <b>Example</b>
     * <pre>
     * calendar.setEntryContentCallback("" +
     * "function(arg) { " +
     * "  let italicEl = document.createElement('i');" +
     * "  if (arg.event.getCustomProperty('isUrgent', false)) {" +
     * "    italicEl.innerHTML = 'urgent event';" +
     * "  } else {" +
     * "    italicEl.innerHTML = 'normal event';" +
     * "  }" +
     * "  let arrayOfDomNodes = [ italicEl ];" +
     * "  return { domNodes: arrayOfDomNodes }" +
     * "}");
     * </pre>
     *
     * @param s function to be attached
     */
    public void setEntryContentCallback(String s) {
        getElement().callJsFunction("setEventContentCallback", s);
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the eventDidMount callback. It must be a valid JavaScript function.
     * <br><br>
     * Called right after the element has been added to the DOM. If the event data changes, this is <b>NOT</b> called again.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     *
     * @param s function to be attached
     */
    public void setEntryDidMountCallback(String s) {
        getElement().callJsFunction("setEventDidMountCallback", s);
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the eventWillUnmount callback. It must be a valid JavaScript function.
     * <br><br>
     * Called right before the element will be removed from the DOM.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     *
     * @param s function to be attached
     */
    public void setEntryWillUnmountCallback(String s) {
        getElement().callJsFunction("setEventWillUnmountCallback", s);
    }

    /**
     * Sets the business hours for this calendar instance. You may pass multiple instances for different configurations.
     * Please be aware, that instances with crossing days or times are handled by the client side and may lead
     * to unexpected results.
     *
     * @param hours hours to set
     * @throws NullPointerException when null is passed
     */
    public void setBusinessHours(@NotNull BusinessHours... hours) {
        Objects.requireNonNull(hours);

        setOption(Option.BUSINESS_HOURS, JsonUtils.toJsonValue(Arrays.stream(hours).map(BusinessHours::toJson)), hours);
    }

    /**
     * Removes the business hours for this calendar instance.
     *
     * @throws NullPointerException when null is passed
     */
    public void removeBusinessHours() {
        setOption(Option.BUSINESS_HOURS, null);
    }

    /**
     * Sets the snap duration for this calendar instance.<p>
     * The default is '00:30'
     *
     * @param duration duration to set in format hh:mm
     * @throws NullPointerException when null is passed
     */
    public void setSnapDuration(@NotNull String duration) {
        Objects.requireNonNull(duration);
        setOption(Option.SNAP_DURATION, duration);
    }

    /**
     * Sets the min time for this calendar instance. This is the first time slot that will be displayed for each day.<p>
     * The default is '00:00:00'
     *
     * @param slotMinTime slotMinTime to set
     * @throws NullPointerException when null is passed
     */
    public void setSlotMinTime(@NotNull LocalTime slotMinTime) {
        Objects.requireNonNull(slotMinTime);
        setOption(Option.SLOT_MIN_TIME, JsonUtils.toJsonValue(slotMinTime != null ? slotMinTime : "00:00:00"));
    }

    /**
     * Returns the fixedWeekCount. By default true.
     *
     * @return fixedWeekCount
     */
    public boolean getFixedWeekCount() {
        return (boolean) getOption(Option.FIXED_WEEK_COUNT).orElse(true);
    }

    /**
     * Determines the number of weeks displayed in a month view.
     * If true, the calendar will always be 6 weeks tall.
     * If false, the calendar will have either 4, 5, or 6 weeks, depending on the month.
     *
     * @param fixedWeekCount
     */
    public void setFixedWeekCount(boolean fixedWeekCount) {
        setOption(Option.FIXED_WEEK_COUNT, fixedWeekCount);
    }

    /**
     * Sets the max time for this calendar instance. This is the last time slot that will be displayed for each day<p>
     * The default is '24:00:00'
     *
     * @param slotMaxTime slotMaxTime to set
     * @throws NullPointerException when null is passed
     */
    public void setSlotMaxTime(@NotNull LocalTime slotMaxTime) {
        Objects.requireNonNull(slotMaxTime);
        setOption(Option.SLOT_MAX_TIME, JsonUtils.toJsonValue(slotMaxTime != null ? slotMaxTime : "24:00:00"));
    }

    /**
     * Returns the current timezone of this calendar. Entries will be displayed related to this timezone.
     * Does not affect the server side times of entries, only their client side displayment.
     *
     * @return time zone
     */
    public Timezone getTimezone() {
        return (Timezone) getOption(Option.TIMEZONE).orElse(Timezone.UTC);
    }

    /**
     * Sets the timezone the calendar shall show. Does not affect the entries directly but only their client side displayment.
     *
     * @param timezone
     */
    public void setTimezone(Timezone timezone) {
        Objects.requireNonNull(timezone);

        Timezone oldTimezone = getTimezone();
        if (!timezone.equals(oldTimezone)) {
            setOption(Option.TIMEZONE, timezone.getClientSideValue(), timezone);
        }
    }

    /**
     * Returns the editable flag. By default true.
     *
     * @return editable editable
     */
    public boolean getEntryDurationEditable() {
        return (boolean) getOption(Option.ENTRY_DURATION_EDITABLE).orElse(true);
    }

    /**
     * Allow events’ durations to be editable through resizing.
     * <p>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setDurationEditable(boolean)}
     *
     * @param editable editable
     */
    public void setEntryDurationEditable(boolean editable) {
        setOption(Option.ENTRY_DURATION_EDITABLE, editable);
    }

    /**
     * Returns the editable flag. By default false.
     *
     * @return editable editable
     */
    public boolean getEntryResizableFromStart() {
        return (boolean) getOption(Option.ENTRY_RESIZABLE_FROM_START).orElse(false);
    }

    /**
     * Whether the user can resize an event from its starting edge.
     *
     * @param editable editable
     */
    public void setEntryResizableFromStart(boolean editable) {
        setOption(Option.ENTRY_RESIZABLE_FROM_START, editable);
    }

    /**
     * Returns the editable flag. By default true.
     *
     * @return editable editable
     */
    public boolean getEntryStartEditable() {
        return (boolean) getOption(Option.ENTRY_START_EDITABLE).orElse(true);
    }

    /**
     * Allow events’ start times to be editable through dragging.
     * <p>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setStartEditable(boolean)}
     *
     * @param editable editable
     */
    public void setEntryStartEditable(boolean editable) {
        setOption(Option.ENTRY_START_EDITABLE, editable);
    }

    /**
     * Returns the editable flag. By default false.
     *
     * @return editable editable
     */
    public boolean getEditable() {
        return (boolean) getOption(Option.EDITABLE).orElse(false);
    }

    /**
     * Determines whether the events on the calendar can be modified.
     * <p>
     * This determines if the events can be dragged and resized.
     * Enables/disables both at the same time.
     * If you don’t want both, use the more specific {@link #setEntryStartEditable(boolean)} and {@link #setEntryDurationEditable(boolean)} instead.
     * <br><br>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setEditable(boolean)}.
     * However, Background Events can not be dragged or resized.
     *
     * @param editable editable
     */
    public void setEditable(boolean editable) {
        setOption(Option.EDITABLE, editable);
    }

    /**
     * Returns the weekends display status. By default true.
     *
     * @return weekends
     */
    public boolean getWeekends() {
        return (boolean) getOption(Option.WEEKENDS).orElse(true);
    }

    /**
     * Whether to include Saturday/Sunday columns in any of the calendar views.
     *
     * @param weekends
     */
    public void setWeekends(boolean weekends) {
        setOption(Option.WEEKENDS, weekends);
    }


    /**
     * display the header.
     *
     * @param header
     */
    public void setHeaderToolbar(Header header) {
        setOption(Option.HEADER_TOOLBAR, header.toJson());
    }

    /**
     * display the footer.
     *
     * @param footer
     */
    public void setFooterToolbar(Footer footer) {
        setOption(Option.FOOTER_TOOLBAR, footer.toJson());
    }

    /**
     * Returns the columnHeader.
     *
     * @return columnHeader
     */
    public boolean getColumnHeader() {
        return (boolean) getOption(Option.COLUMN_HEADER).orElse(true);
    }

    /**
     * Whether the day headers should appear. For the Month, TimeGrid, and DayGrid views.
     *
     * @param columnHeader
     */
    public void setColumnHeader(boolean columnHeader) {
        setOption(Option.COLUMN_HEADER, columnHeader);
    }

    /**
     * This method returns the timezone sent by the browser. It is <b>not</b> automatically set as the FC's timezone,
     * except for when the FC builder has been used with the auto timezone parameter.
     * <p/>
     * Is empty if there was no timezone obtainable or the instance has not been attached to the client side, yet.
     *
     * @return optional client side timezone
     */
    public Optional<Timezone> getBrowserTimezone() {
        return Optional.ofNullable(browserTimezone);
    }

    /**
     * Sets the browser time zone. This method is intended to be used by the client only.
     *
     * @param timezoneId timezone id
     */
    @ClientCallable
    protected void setBrowserTimezone(String timezoneId) {
        if (timezoneId != null) {
            this.browserTimezone = new Timezone(ZoneId.of(timezoneId));
            getEventBus().fireEvent(new BrowserTimezoneObtainedEvent(this, false, browserTimezone));
        }
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull Option option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull Option option, boolean forceClientSideValue) {
        return getOption(option.getOptionKey(), forceClientSideValue);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull String option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     * <br><br>
     * Returns {@code null} for initial options. Please use #getRawOption(String)
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull String option, boolean forceClientSideValue) {
        Objects.requireNonNull(option);
        if (!forceClientSideValue && serverSideOptions.containsKey(option)) {
            return Optional.ofNullable((T) serverSideOptions.get(option));
        }

        return Optional.ofNullable((T) options.get(option));
//        return Optional.ofNullable((T) options.get(option));
    }

//    /**
//     * Tries to get the current option from the client. Opposite to the other {@code getOption(...)} methods, this
//     * method does not refer to internal caches of the server, but uses the client side only. This means, that
//     * the returned value can be manipulated by the client. You should use this method only for test purposes.
//     *
//     * @param option option key
//     * @param <T> return type to be expected
//     * @return optional
//     * @throws ExecutionException when an error has occured execution the client side call
//     * @throws InterruptedException when the client side call has been interrupted
//     */
//    public <T> Optional<T> getRawClientSideOption(@NotNull String option) throws ExecutionException, InterruptedException {
//        return (Optional<T>) Optional.ofNullable(getElement().callJsFunction("getOption", option).toCompletableFuture().get().toNative());
//    }


    /**
     * Force the client side instance to re-render it's content.
     */
    public void render() {
        getElement().callJsFunction("render");
    }
    
    /**
     * This method set a vaadin component as draggable and can interact with the calendar.
     * When you drop an external entry on the calendar the drop event will fire, or the eventReceive.
     * 
     * You can access those methods with calendar.addExternalEntryDroppedListener and calendar.addExternalEntryReveicedListener.
     * 
     * @param draggableComponent
     * @param externalEntryDetails
     */
    public void setExternalDraggableElement(@NotNull Component draggableComponent, @NotNull Entry externalEntryDetails) {
    	Objects.requireNonNull(draggableComponent);
    	Objects.requireNonNull(externalEntryDetails);
   	 
    	getElement().callJsFunction("setExternalDraggableElement", draggableComponent, externalEntryDetails.toJson());
    }

    /**
     * Registers a listener to be informed when an external entry dropped event occurred.
    *
    * @param listener listener
    * @return registration to remove the listener
    * @throws NullPointerException when null is passed
    */
   public Registration addExternalEntryReceivedListener(@NotNull ComponentEventListener<ExternalEntryReceivedEvent> listener) {
       Objects.requireNonNull(listener);
       return addListener(ExternalEntryReceivedEvent.class, listener);
   }
   
   public Registration addDroppedListener(@NotNull ComponentEventListener<DroppedEvent> listener) {
       Objects.requireNonNull(listener);
       return addListener(DroppedEvent.class, listener);
   }

    /**
     * Registers a listener to be informed when a timeslot click event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotClickedListener(@NotNull ComponentEventListener<? extends TimeslotClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedEvent.class, (ComponentEventListener<TimeslotClickedEvent>) listener);
    }

    /**
     * Registers a listener to be informed when an entry click event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryClickedListener(@NotNull ComponentEventListener<EntryClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryClickedEvent.class, listener);
    }
    
    /**
     * Registers a listener to be informed when the user mouses over an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryMouseEnterListener(@NotNull ComponentEventListener<EntryMouseEnterEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryMouseEnterEvent.class, listener);
    }
    
    /**
     * Registers a listener to be informed when the user mouses out of an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryMouseLeaveListener(@NotNull ComponentEventListener<EntryMouseLeaveEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryMouseLeaveEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry resized event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryResizedListener(@NotNull ComponentEventListener<EntryResizedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry dropped event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryDroppedListener(@NotNull ComponentEventListener<EntryDroppedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDroppedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when a dates rendered event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addDatesRenderedListener(@NotNull ComponentEventListener<DatesRenderedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DatesRenderedEvent.class, listener);
    }


    /**
     * Registers a listener to be informed when a view skeleton rendered event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addViewSkeletonRenderedListener(@NotNull ComponentEventListener<ViewSkeletonRenderedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(ViewSkeletonRenderedEvent.class, listener);
    }


    /**
     * Registers a listener to be informed when the user selected a range of timeslots.
     * <br><br>
     * You should deactivate timeslot clicked listeners since both events will get fired when the user only selects
     * one timeslot / day.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotsSelectedListener(@NotNull ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedEvent.class, (ComponentEventListener<TimeslotsSelectedEvent>) listener);
    }

    /**
     * Registers a listener to be informed when the user clicked on the "more" link (e.g. "+6 more").
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addMoreLinkClickedListener(@NotNull ComponentEventListener<MoreLinkClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(MoreLinkClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed, when a user clicks a day's number.
     * <br><br>
     * {@link #setNumberClickable(boolean)} needs to be called with true before.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addDayNumberClickedListener(@NotNull ComponentEventListener<DayNumberClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DayNumberClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed, when a user clicks a week's number.
     * <br><br>
     * {@link #setNumberClickable(boolean)} needs to be called with true before.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addWeekNumberClickedListener(@NotNull ComponentEventListener<WeekNumberClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(WeekNumberClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed, when the browser's timezone has been obtained by the server.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addBrowserTimezoneObtainedListener(@NotNull ComponentEventListener<BrowserTimezoneObtainedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(BrowserTimezoneObtainedEvent.class, listener);
    }

    /**
     * This method allows to add a custom css string to the full calendar to customize its styling without
     * the need of subclassing the client side or using css properties.
     * <br><br>
     * The given string is set as the innerHTML of a client side styles element. <b>Attention:</b> The given
     * string is taken as it is. Please be advised, that this method can be used to introduce malicious code into your
     * page, so you should be sure, that the added css code is safe (e.g. not taken from user input or the databse).
     *
     * @param customStylesString custom css string
     */
    public void addCustomStyles(String customStylesString) {
        getElement().callJsFunction("addCustomStyles", customStylesString);
    }

    /**
     * Sets an action, that shall happen, when a user clicks the "+x more" link in the calendar (which occurs when the max
     * entries per day are exceeded). Default value is {@code POPUP}. Passing {@code null} will reset the default.
     *
     * @param moreLinkClickAction action to set
     * @see MoreLinkClickAction
     */
    public void setMoreLinkClickAction(MoreLinkClickAction moreLinkClickAction) {
        getElement().setProperty("moreLinkClickAction", (moreLinkClickAction != null ? moreLinkClickAction : MoreLinkClickAction.POPUP).getClientSideValue());
    }

    /**
     * Enumeration of possible options, that can be applied to this calendar instance to have an effect on the client side.
     * This list does not contain all options, but the most common used ones.
     * <br><br>
     * Please refer to the FullCalendar client library documentation for possible options:
     * https://fullcalendar.io/docs
     */
    public enum Option {
        FIRST_DAY("firstDay"),
        HEIGHT("height"),
        LOCALE("locale"),
        SELECTABLE("selectable"),
        WEEK_NUMBERS("weekNumbers"),
        NOW_INDICATOR("nowIndicator"),
        NAV_LINKS("navLinks"),
        BUSINESS_HOURS("businessHours"),
        TIMEZONE("timeZone"),
        SNAP_DURATION("snapDuration"),
        SLOT_MIN_TIME("slotMinTime"),
        SLOT_MAX_TIME("slotMaxTime"),
        FIXED_WEEK_COUNT("fixedWeekCount"),
        WEEKENDS("weekends"),
        HEADER_TOOLBAR("headerToolbar"),
        FOOTER_TOOLBAR("footerToolbar"),
        COLUMN_HEADER("columnHeader"),
        WEEK_NUMBERS_WITHIN_DAYS("weekNumbersWithinDays"),
        ENTRY_DURATION_EDITABLE("eventDurationEditable"),
        ENTRY_RESIZABLE_FROM_START("eventResizableFromStart"),
        ENTRY_START_EDITABLE("eventStartEditable"),
        ENTRY_TIME_FORMAT("eventTimeFormat"),
        EDITABLE("editable");

        private final String optionKey;

        Option(String optionKey) {
            this.optionKey = optionKey;
        }

        String getOptionKey() {
            return optionKey;
        }
    }

    /**
     * Possible actions, that shall happen on the client side.
     */
    public enum MoreLinkClickAction implements ClientSideValue {
        /**
         * Shows a popup on the client side. This popup is purely client side rendered. Entries shown in that
         * popup will fire the same click event as "normal" entries do.
         */
        POPUP("popover"),
        /**
         * Goes to a "day" view based on the current one.
         */
        DAY("day"),

        /**
         * Goes to a "week" view based on the current one.
         */
        WEEK("week"),

        /**
         * Nothing will happen automatically. You should use this action if you want to handle
         * the action manually (e.g. showing your own dialog / popup for the given events).
         */
        NOTHING("function");

        private final String clientSideValue;

        MoreLinkClickAction(String clientSideValue) {
            this.clientSideValue = clientSideValue;
        }

        @Override
        public String getClientSideValue() {
            return this.clientSideValue;
        }
    }

}
