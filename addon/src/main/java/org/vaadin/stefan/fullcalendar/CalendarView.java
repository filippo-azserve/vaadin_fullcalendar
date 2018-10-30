package org.vaadin.stefan.fullcalendar;

/**
 * Enumeration of possible calendar views.
 */
public enum CalendarView {
    MONTH("month"),
    AGENDA_DAY("agendaDay"),
    AGENDA_WEEK("agendaWeek"),
    BASIC_DAY("basicDay"),
    BASIC_WEEK("basicWeek"),
    LIST_WEEK("listWeek"),
    LIST_DAY("listDay"),
    LIST_MONTH("listMonth"),
    LIST_YEAR("listYear"),
    ;

    private final String clientSideName;

    CalendarView(String clientSideName) {
        this.clientSideName = clientSideName;
    }

    public String getClientSideName() {
        return clientSideName;
    }
}