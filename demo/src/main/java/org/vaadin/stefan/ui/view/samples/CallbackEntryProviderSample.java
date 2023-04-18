package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

/**
 * @author Stefan Uebe
 */
public class CallbackEntryProviderSample extends AbstractSample {

    private final EntryService<Entry> backend = EntryService.createInstance();

    @Override
    protected void buildSample(FullCalendar calendar) {
        // the callback provider uses the given callback to fetch entries when necessary
        CallbackEntryProvider<Entry> entryProvider = EntryProvider.fromCallbacks(
                backend::streamEntries,
                entryId -> backend.getEntry(entryId).orElse(null)
        );

        // set entry provider
        calendar.setEntryProvider(entryProvider);

        // CRUD operations
        // to add
        Entry entry = new Entry();          // ... plus some init
        backend.addEntries(entry);            // register in your backend
        entryProvider.refreshAll();         // call refresh to inform the client about the data change and trigger a refetch

        // after some change
        backend.updateEntry(entry);         // inform your backend
        entryProvider.refreshItem(entry);   // call refresh to inform the client about the data change and trigger a refetch

        // to remove
        backend.removeEntries(entry);         // remove from your backend
        entryProvider.refreshAll();   // call refresh to inform the client about the data change and trigger a refetch
    }
}