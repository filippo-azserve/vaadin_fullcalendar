/*
   Copyright 2020, Stefan Uebe

   Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
   documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
   rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
   permit persons to whom the Software is furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all copies or substantial portions
   of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

   Exception of this license is the separately licensed part of the styles.
*/
import {html} from '@polymer/polymer/polymer-element.js';
import {FullCalendar} from './full-calendar.js';
import resourceTimelinePlugin from '@fullcalendar/resource-timeline';
import resourceTimeGridPlugin from '@fullcalendar/resource-timegrid';

export class FullCalendarScheduler extends FullCalendar {
    static get is() {
        return 'full-calendar-scheduler';
    }

    static get template() {
    	
        return html`
            ${this.templateCalendarCss}
        	${this.templateSchedulerCss}
            ${this.templateElementCss}
        
            ${this.templateContainer}
        `;
    }

    static get templateSchedulerCss() {
        return html`
        <style>
        	.fc-cell-shaded,.fc .fc-day-disabled{background:rgba(208,208,208,.3);background:var(--fc-neutral-bg-color,rgba(208,208,208,.3))}
			.fc-resource-timeline-divider{width:3px;cursor:col-resize}
			.fc-resource-timeline-flat .fc-datagrid-expander-placeholder{display:none}
			.fc-resource-timeline .fc-resource-group:not([rowspan]){background:rgba(208,208,208,.3);background:var(--fc-neutral-bg-color,rgba(208,208,208,.3))}
			.fc-timeline-lane-frame{position:relative}
			
			.fc-timeline-events{position:relative;z-index:3;width:0}
			.fc-timeline-event-harness{position:absolute;top:0}
			.fc-timeline-event{z-index:1;position:relative;display:flex;align-items:center;border-radius:0;padding:2px 1px;margin-bottom:1px;font-size:.85em;font-size:var(--fc-small-font-size,.85em)}
			.fc-timeline-bg, .fc-timeline-bg-harness{position:absolute;top:0;bottom:0}
			.fc-timeline-event.fc-event-mirror{z-index:2}
			.fc-timeline-event .fc-event-main{flex-grow:1;flex-shrink:1;min-width:0}
			.fc-timeline-event .fc-event-time{font-weight:700}
			.fc-timeline-event .fc-event-time,.fc-timeline-event .fc-event-title{white-space:nowrap;padding:0 2px}
			.fc-direction-ltr .fc-timeline-event.fc-event-end{margin-right:1px}
			.fc-direction-rtl .fc-timeline-event.fc-event-end{margin-left:1px}
			.fc-timeline-overlap-disabled .fc-timeline-event{padding-top:5px;padding-bottom:5px;margin-bottom:0}
			.fc-timeline-event:not(.fc-event-end):after,.fc-timeline-event:not(.fc-event-start):before{content:"";flex-grow:0;flex-shrink:0;opacity:.5;width:0;height:0;margin:0 1px;border:5px solid #000;border-top-color:transparent;border-bottom-color:transparent}
			.fc-direction-ltr .fc-timeline-event:not(.fc-event-start):before,.fc-direction-rtl .fc-timeline-event:not(.fc-event-end):after{border-left:0}
			.fc-direction-ltr .fc-timeline-event:not(.fc-event-end):after,.fc-direction-rtl .fc-timeline-event:not(.fc-event-start):before{border-right:0}
			.fc-timeline-bg{z-index:2;width:0;left:0;right:0}
			.fc-timeline-bg .fc-non-business{z-index:1}
			.fc-timeline-bg .fc-bg-event{z-index:2}
			.fc-timeline-bg .fc-highlight{z-index:3}
			.fc-resource-timeline-divider{width:3px;cursor:col-resize}
			.fc-resource-timeline .fc-resource-group:not([rowspan]){background:rgba(208,208,208,.3);background:var(--fc-neutral-bg-color,rgba(208,208,208,.3))}
        	.fc-timeline-lane-frame{position:relative}
        	.fc-timeline-overlap-enabled .fc-timeline-lane-frame .fc-timeline-events{box-sizing:content-box;padding-bottom:10px}
			
			
			.fc-ltr .fc-time-area .fc-bgevent-container { left: var(--fc-ltr_fc-time-area_fc-bgevent-container-left, 0); }
			.fc-ltr .fc-time-area .fc-chrono th { text-align: var(--fc-ltr_fc-time-area_fc-chrono_th-text-align, left); }
			.fc-ltr .fc-time-area .fc-highlight-container { left: var(--fc-ltr_fc-time-area_fc-highlight-container-left, 0); }
			.fc-ltr .fc-time-area .fc-mirror-container { left: var(--fc-ltr_fc-time-area_fc-mirror-container-left, 0); }
			.fc-ltr .fc-time-area .fc-slats td { border-right-width: var(--fc-ltr_fc-time-area_fc-slats_td-border-right-width, 0); }
			.fc-ltr .fc-timeline-event { flex-direction: var(--fc-ltr_fc-timeline-event-flex-direction, row); }
			.fc-ltr .fc-timeline-event { margin-right: var(--fc-ltr_fc-timeline-event-margin-right, 1px); }
			.fc-ltr .fc-timeline-event.fc-not-end:after { border-right: var(--fc-ltr_fc-timeline-eventfc-not-end_COLON_after-border-right, 0); }
			.fc-ltr .fc-timeline-event.fc-not-start:before { border-left: var(--fc-ltr_fc-timeline-eventfc-not-start_COLON_before-border-left, 0); }
			.fc-no-overlap .fc-time-area .fc-event-container { padding-bottom: var(--fc-no-overlap_fc-time-area_fc-event-container-padding-bottom, 0); }
			.fc-no-overlap .fc-time-area .fc-event-container { top: var(--fc-no-overlap_fc-time-area_fc-event-container-top, 0); }
			.fc-no-overlap .fc-timeline-event { margin-bottom: var(--fc-no-overlap_fc-timeline-event-margin-bottom, 0); }
			.fc-no-overlap .fc-timeline-event { padding-bottom: var(--fc-no-overlap_fc-timeline-event-padding-bottom, 5px); }
			.fc-no-overlap .fc-timeline-event { padding-top: var(--fc-no-overlap_fc-timeline-event-padding-top, 5px); }
			.fc-no-scrollbars { background: var(--fc-no-scrollbars-background, rgba(255, 255, 255, 0)); }
			.fc-no-scrollbars::-webkit-scrollbar { height: var(--fc-no-scrollbars_COLON__COLON_-webkit-scrollbar-height, 0); }
			.fc-no-scrollbars::-webkit-scrollbar { width: var(--fc-no-scrollbars_COLON__COLON_-webkit-scrollbar-width, 0); }
			.fc-rtl .fc-time-area .fc-bgevent-container { right: var(--fc-rtl_fc-time-area_fc-bgevent-container-right, 0); }
			.fc-rtl .fc-time-area .fc-chrono th { text-align: var(--fc-rtl_fc-time-area_fc-chrono_th-text-align, right); }
			.fc-rtl .fc-time-area .fc-highlight-container { right: var(--fc-rtl_fc-time-area_fc-highlight-container-right, 0); }
			.fc-rtl .fc-time-area .fc-mirror-container { right: var(--fc-rtl_fc-time-area_fc-mirror-container-right, 0); }
			.fc-rtl .fc-time-area .fc-slats td { border-left-width: var(--fc-rtl_fc-time-area_fc-slats_td-border-left-width, 0); }
			.fc-rtl .fc-timeline { direction: var(--fc-rtl_fc-timeline-direction, rtl); }
			.fc-rtl .fc-timeline-event { margin-left: var(--fc-rtl_fc-timeline-event-margin-left, 1px); }
			.fc-rtl .fc-timeline-event.fc-not-end:after { border-left: var(--fc-rtl_fc-timeline-eventfc-not-end_COLON_after-border-left, 0); }
			.fc-rtl .fc-timeline-event.fc-not-start:before { border-right: var(--fc-rtl_fc-timeline-eventfc-not-start_COLON_before-border-right, 0); }
			.fc-scrolled .fc-head .fc-scroller { z-index: var(--fc-scrolled_fc-head_fc-scroller-z-index, 2); }
			.fc-scroller-canvas > .fc-bg { z-index: var(--fc-scroller-canvas__LACE_BRACE__fc-bg-z-index, 1); }
			.fc-scroller-canvas > .fc-content { border-style: var(--fc-scroller-canvas__LACE_BRACE__fc-content-border-style, solid); }
			.fc-scroller-canvas > .fc-content { border-width: var(--fc-scroller-canvas__LACE_BRACE__fc-content-border-width, 0); }
			.fc-scroller-canvas > .fc-content { position: var(--fc-scroller-canvas__LACE_BRACE__fc-content-position, relative); }
			.fc-scroller-canvas > .fc-content { z-index: var(--fc-scroller-canvas__LACE_BRACE__fc-content-z-index, 2); }
			.fc-scroller-canvas { box-sizing: var(--fc-scroller-canvas-box-sizing, border-box); }
			.fc-scroller-canvas { min-height: var(--fc-scroller-canvas-min-height, 100%); }
			.fc-scroller-canvas { position: var(--fc-scroller-canvas-position, relative); }
			.fc-scroller-canvas.fc-gutter-left > .fc-content { border-left-width: var(--fc-scroller-canvasfc-gutter-left__LACE_BRACE__fc-content-border-left-width, 1px); }
			.fc-scroller-canvas.fc-gutter-left > .fc-content { margin-left: var(--fc-scroller-canvasfc-gutter-left__LACE_BRACE__fc-content-margin-left, -1px); }
			.fc-scroller-canvas.fc-gutter-right > .fc-content { border-right-width: var(--fc-scroller-canvasfc-gutter-right__LACE_BRACE__fc-content-border-right-width, 1px); }
			.fc-scroller-canvas.fc-gutter-right > .fc-content { margin-right: var(--fc-scroller-canvasfc-gutter-right__LACE_BRACE__fc-content-margin-right, -1px); }
			.fc-scroller-canvas.fc-gutter-top > .fc-content { border-top-width: var(--fc-scroller-canvasfc-gutter-top__LACE_BRACE__fc-content-border-top-width, 1px); }
			.fc-scroller-canvas.fc-gutter-top > .fc-content { margin-top: var(--fc-scroller-canvasfc-gutter-top__LACE_BRACE__fc-content-margin-top, -1px); }
			.fc-scroller-clip { overflow: var(--fc-scroller-clip-overflow, hidden); }
			.fc-scroller-clip { position: var(--fc-scroller-clip-position, relative); }
			.fc-time-area .fc-bgevent { bottom: var(--fc-time-area_fc-bgevent-bottom, 0); }
			.fc-time-area .fc-bgevent { position: var(--fc-time-area_fc-bgevent-position, absolute); }
			.fc-time-area .fc-bgevent { top: var(--fc-time-area_fc-bgevent-top, 0); }
			.fc-time-area .fc-bgevent-container { bottom: var(--fc-time-area_fc-bgevent-container-bottom, 0); }
			.fc-time-area .fc-bgevent-container { position: var(--fc-time-area_fc-bgevent-container-position, absolute); }
			.fc-time-area .fc-bgevent-container { top: var(--fc-time-area_fc-bgevent-container-top, 0); }
			.fc-time-area .fc-bgevent-container { width: var(--fc-time-area_fc-bgevent-container-width, 0); }
			.fc-time-area .fc-bgevent-container { z-index: var(--fc-time-area_fc-bgevent-container-z-index, 2); }
			.fc-time-area .fc-event-container { padding-bottom: var(--fc-time-area_fc-event-container-padding-bottom, 8px); }
			.fc-time-area .fc-event-container { position: var(--fc-time-area_fc-event-container-position, relative); }
			.fc-time-area .fc-event-container { top: var(--fc-time-area_fc-event-container-top, -1px); }
			.fc-time-area .fc-event-container { width: var(--fc-time-area_fc-event-container-width, 0); }
			.fc-time-area .fc-event-container { z-index: var(--fc-time-area_fc-event-container-z-index, 2); }
			.fc-time-area .fc-highlight { bottom: var(--fc-time-area_fc-highlight-bottom, 0); }
			.fc-time-area .fc-highlight { position: var(--fc-time-area_fc-highlight-position, absolute); }
			.fc-time-area .fc-highlight { top: var(--fc-time-area_fc-highlight-top, 0); }
			.fc-time-area .fc-highlight-container { bottom: var(--fc-time-area_fc-highlight-container-bottom, 0); }
			.fc-time-area .fc-highlight-container { position: var(--fc-time-area_fc-highlight-container-position, absolute); }
			.fc-time-area .fc-highlight-container { top: var(--fc-time-area_fc-highlight-container-top, 0); }
			.fc-time-area .fc-highlight-container { width: var(--fc-time-area_fc-highlight-container-width, 0); }
			.fc-time-area .fc-highlight-container { z-index: var(--fc-time-area_fc-highlight-container-z-index, 2); }
			.fc-time-area .fc-mirror-container { position: var(--fc-time-area_fc-mirror-container-position, absolute); }
			.fc-time-area .fc-mirror-container { top: var(--fc-time-area_fc-mirror-container-top, 0); }
			.fc-time-area .fc-mirror-container { z-index: var(--fc-time-area_fc-mirror-container-z-index, 3); }
			.fc-time-area .fc-now-indicator-arrow { border-left-color: var(--fc-time-area_fc-now-indicator-arrow-border-left-color, transparent); }
			.fc-time-area .fc-now-indicator-arrow { border-right-color: var(--fc-time-area_fc-now-indicator-arrow-border-right-color, transparent); }
			.fc-time-area .fc-now-indicator-arrow { border-width: var(--fc-time-area_fc-now-indicator-arrow-border-width, 6px 5px 0 5px); }
			.fc-time-area .fc-now-indicator-arrow { margin: var(--fc-time-area_fc-now-indicator-arrow-margin, 0 -6px); }
			.fc-time-area .fc-now-indicator-line { border-left-width: var(--fc-time-area_fc-now-indicator-line-border-left-width, 1px); }
			.fc-time-area .fc-now-indicator-line { bottom: var(--fc-time-area_fc-now-indicator-line-bottom, 0); }
			.fc-time-area .fc-now-indicator-line { margin: var(--fc-time-area_fc-now-indicator-line-margin, 0 -1px); }
			.fc-time-area .fc-slats .fc-minor { border-style: var(--fc-time-area_fc-slats_fc-minor-border-style, dotted); }
			.fc-time-area .fc-slats table { height: var(--fc-time-area_fc-slats_table-height, 100%); }
			.fc-time-area .fc-slats td { border-width: var(--fc-time-area_fc-slats_td-border-width, 0 1px); }
			.fc-time-area .fc-slats { bottom: var(--fc-time-area_fc-slats-bottom, 0); }
			.fc-time-area .fc-slats { left: var(--fc-time-area_fc-slats-left, 0); }
			.fc-time-area .fc-slats { position: var(--fc-time-area_fc-slats-position, absolute); }
			.fc-time-area .fc-slats { right: var(--fc-time-area_fc-slats-right, 0); }
			.fc-time-area .fc-slats { top: var(--fc-time-area_fc-slats-top, 0); }
			.fc-time-area .fc-slats { z-index: var(--fc-time-area_fc-slats-z-index, 1); }
			.fc-time-area col { min-width: var(--fc-time-area_col-min-width, 2.2em); }
			.fc-time-area tr:first-child .fc-event-container { top: var(--fc-time-area_tr_COLON_first-child_fc-event-container-top, 0); }
			.fc-timeline .fc-body .fc-scroller { z-index: var(--fc-timeline_fc-body_fc-scroller-z-index, 1); }
			.fc-timeline .fc-cell-content { overflow: var(--fc-timeline_fc-cell-content-overflow, hidden); }
			.fc-timeline .fc-cell-text { display: var(--fc-timeline_fc-cell-text-display, inline-block); }
			.fc-timeline .fc-cell-text { padding-left: var(--fc-timeline_fc-cell-text-padding-left, 4px); }
			.fc-timeline .fc-cell-text { padding-right: var(--fc-timeline_fc-cell-text-padding-right, 4px); }
			.fc-timeline .fc-head .fc-cell-content { padding-bottom: var(--fc-timeline_fc-head_fc-cell-content-padding-bottom, 3px); }
			.fc-timeline .fc-head .fc-cell-content { padding-top: var(--fc-timeline_fc-head_fc-cell-content-padding-top, 3px); }
			.fc-timeline .fc-head .fc-time-area .fc-cell-content { overflow: var(--fc-timeline_fc-head_fc-time-area_fc-cell-content-overflow, visible); }
			.fc-timeline .fc-now-indicator { top: var(--fc-timeline_fc-now-indicator-top, 0); }
			.fc-timeline .fc-now-indicator { z-index: var(--fc-timeline_fc-now-indicator-z-index, 3); }
			.fc-timeline .fc-scroller-canvas > .fc-content > .fc-rows > table { border-bottom-style: var(--fc-timeline_fc-scroller-canvas__LACE_BRACE__fc-content__LACE_BRACE__fc-rows__LACE_BRACE__table-border-bottom-style, none); }
			.fc-timeline .fc-scroller-canvas > div > div > table { border-style: var(--fc-timeline_fc-scroller-canvas__LACE_BRACE__div__LACE_BRACE__div__LACE_BRACE__table-border-style, hidden); }
			.fc-timeline .fc-scroller-canvas > div > table { border-style: var(--fc-timeline_fc-scroller-canvas__LACE_BRACE__div__LACE_BRACE__table-border-style, hidden); }
			.fc-timeline td { white-space: var(--fc-timeline_td-white-space, nowrap); }
			.fc-timeline th { vertical-align: var(--fc-timeline_th-vertical-align, middle); }
			.fc-timeline th { white-space: var(--fc-timeline_th-white-space, nowrap); }
			.fc-timeline-event .fc-time { -moz-box-sizing: var(--fc-timeline-event_fc-time--moz-box-sizing, border-box); }
			.fc-timeline-event .fc-time { -webkit-box-sizing: var(--fc-timeline-event_fc-time--webkit-box-sizing, border-box); }
			.fc-timeline-event .fc-time { box-sizing: var(--fc-timeline-event_fc-time-box-sizing, border-box); }
			.fc-timeline-event .fc-time { display: var(--fc-timeline-event_fc-time-display, inline-block); }
			.fc-timeline-event .fc-time { font-weight: var(--fc-timeline-event_fc-time-font-weight, bold); }
			.fc-timeline-event .fc-time { max-width: var(--fc-timeline-event_fc-time-max-width, 100%); }
			.fc-timeline-event .fc-time { overflow: var(--fc-timeline-event_fc-time-overflow, hidden); }
			.fc-timeline-event .fc-time { padding: var(--fc-timeline-event_fc-time-padding, 0 2px); }
			.fc-timeline-event .fc-time { vertical-align: var(--fc-timeline-event_fc-time-vertical-align, top); }
			.fc-timeline-event .fc-time { white-space: var(--fc-timeline-event_fc-time-white-space, nowrap); }
			.fc-timeline-event .fc-time-wrap { flex-shrink: var(--fc-timeline-event_fc-time-wrap-flex-shrink, 0); }
			.fc-timeline-event .fc-time-wrap { min-width: var(--fc-timeline-event_fc-time-wrap-min-width, 0); }
			.fc-timeline-event .fc-title { -moz-box-sizing: var(--fc-timeline-event_fc-title--moz-box-sizing, border-box); }
			.fc-timeline-event .fc-title { -webkit-box-sizing: var(--fc-timeline-event_fc-title--webkit-box-sizing, border-box); }
			.fc-timeline-event .fc-title { box-sizing: var(--fc-timeline-event_fc-title-box-sizing, border-box); }
			.fc-timeline-event .fc-title { display: var(--fc-timeline-event_fc-title-display, inline-block); }
			.fc-timeline-event .fc-title { max-width: var(--fc-timeline-event_fc-title-max-width, 100%); }
			.fc-timeline-event .fc-title { overflow: var(--fc-timeline-event_fc-title-overflow, hidden); }
			.fc-timeline-event .fc-title { padding: var(--fc-timeline-event_fc-title-padding, 0 2px); }
			.fc-timeline-event .fc-title { vertical-align: var(--fc-timeline-event_fc-title-vertical-align, top); }
			.fc-timeline-event .fc-title { white-space: var(--fc-timeline-event_fc-title-white-space, nowrap); }
			.fc-timeline-event .fc-title-wrap { flex-grow: var(--fc-timeline-event_fc-title-wrap-flex-grow, 1); }
			.fc-timeline-event .fc-title-wrap { min-width: var(--fc-timeline-event_fc-title-wrap-min-width, 0); }
			.fc-timeline-event { border-radius: var(--fc-timeline-event-border-radius, 0); }
			.fc-timeline-event { display: var(--fc-timeline-event-display, flex); }
			.fc-timeline-event { margin-bottom: var(--fc-timeline-event-margin-bottom, 1px); }
			.fc-timeline-event { padding: var(--fc-timeline-event-padding, 2px 1px); }
			.fc-timeline-event { position: var(--fc-timeline-event-position, absolute); }
			.fc-timeline-event.fc-not-end:after { align-self: var(--fc-timeline-eventfc-not-end_COLON_after-align-self, center); }
			.fc-timeline-event.fc-not-end:after { border-bottom-color: var(--fc-timeline-eventfc-not-end_COLON_after-border-bottom-color, transparent); }
			.fc-timeline-event.fc-not-end:after { border-top-color: var(--fc-timeline-eventfc-not-end_COLON_after-border-top-color, transparent); }
			.fc-timeline-event.fc-not-end:after { border: var(--fc-timeline-eventfc-not-end_COLON_after-border, 5px solid rgb(0, 0, 0)); }
			.fc-timeline-event.fc-not-end:after { content: var(--fc-timeline-eventfc-not-end_COLON_after-content, \"\"); }
			.fc-timeline-event.fc-not-end:after { height: var(--fc-timeline-eventfc-not-end_COLON_after-height, 0); }
			.fc-timeline-event.fc-not-end:after { margin: var(--fc-timeline-eventfc-not-end_COLON_after-margin, 0 1px); }
			.fc-timeline-event.fc-not-end:after { opacity: var(--fc-timeline-eventfc-not-end_COLON_after-opacity, 0.5); }
			.fc-timeline-event.fc-not-end:after { width: var(--fc-timeline-eventfc-not-end_COLON_after-width, 0); }
			.fc-timeline-event.fc-not-start:before { align-self: var(--fc-timeline-eventfc-not-start_COLON_before-align-self, center); }
			.fc-timeline-event.fc-not-start:before { border-bottom-color: var(--fc-timeline-eventfc-not-start_COLON_before-border-bottom-color, transparent); }
			.fc-timeline-event.fc-not-start:before { border-top-color: var(--fc-timeline-eventfc-not-start_COLON_before-border-top-color, transparent); }
			.fc-timeline-event.fc-not-start:before { border: var(--fc-timeline-eventfc-not-start_COLON_before-border, 5px solid rgb(0, 0, 0)); }
			.fc-timeline-event.fc-not-start:before { content: var(--fc-timeline-eventfc-not-start_COLON_before-content, \"\"); }
			.fc-timeline-event.fc-not-start:before { height: var(--fc-timeline-eventfc-not-start_COLON_before-height, 0); }
			.fc-timeline-event.fc-not-start:before { margin: var(--fc-timeline-eventfc-not-start_COLON_before-margin, 0 1px); }
			.fc-timeline-event.fc-not-start:before { opacity: var(--fc-timeline-eventfc-not-start_COLON_before-opacity, 0.5); }
			.fc-timeline-event.fc-not-start:before { width: var(--fc-timeline-eventfc-not-start_COLON_before-width, 0); }
			.fc-timeline.fc-scrolled .fc-head .fc-scroller { box-shadow: var(--fc-timelinefc-scrolled_fc-head_fc-scroller-box-shadow, 0 3px 4px rgba(0, 0, 0, 0.075)); }
			.fc-body .fc-resource-area .fc-cell-content { padding-bottom: var(--fc-body_fc-resource-area_fc-cell-content-padding-bottom, 8px); }
			.fc-body .fc-resource-area .fc-cell-content { padding-top: var(--fc-body_fc-resource-area_fc-cell-content-padding-top, 8px); }
			.fc-flat .fc-expander-space { display: var(--fc-flat_fc-expander-space-display, none); }
			.fc-ltr .fc-resource-area th .fc-col-resizer { right: var(--fc-ltr_fc-resource-area_th_fc-col-resizer-right, -3px); }
			.fc-ltr .fc-resource-area tr > * { text-align: var(--fc-ltr_fc-resource-area_tr__LACE_BRACE___ASTERISK_-text-align, left); }
			.fc-no-overlap .fc-body .fc-resource-area .fc-cell-content { padding-bottom: var(--fc-no-overlap_fc-body_fc-resource-area_fc-cell-content-padding-bottom, 6px); }
			.fc-no-overlap .fc-body .fc-resource-area .fc-cell-content { padding-top: var(--fc-no-overlap_fc-body_fc-resource-area_fc-cell-content-padding-top, 6px); }
			.fc-resource-area .fc-cell-content { padding-left: var(--fc-resource-area_fc-cell-content-padding-left, 4px); }
			.fc-resource-area .fc-cell-content { padding-right: var(--fc-resource-area_fc-cell-content-padding-right, 4px); }
			.fc-resource-area .fc-expander { cursor: var(--fc-resource-area_fc-expander-cursor, pointer); }
			.fc-resource-area .fc-expander { opacity: var(--fc-resource-area_fc-expander-opacity, 0.65); }
			.fc-resource-area .fc-icon { display: var(--fc-resource-area_fc-icon-display, inline-block); }
			.fc-resource-area .fc-icon { text-align: var(--fc-resource-area_fc-icon-text-align, center); }
			.fc-resource-area .fc-icon { width: var(--fc-resource-area_fc-icon-width, 1em); }
			.fc-resource-area .fc-super th { text-align: var(--fc-resource-area_fc-super_th-text-align, center); }
			.fc-resource-area col { min-width: var(--fc-resource-area_col-min-width, 70px); }
			.fc-resource-area col { width: var(--fc-resource-area_col-width, 40%); }
			.fc-resource-area col.fc-main-col { width: var(--fc-resource-area_colfc-main-col-width, 60%); }
			.fc-resource-area th .fc-cell-content { position: var(--fc-resource-area_th_fc-cell-content-position, relative); }
			.fc-resource-area th .fc-cell-content { z-index: var(--fc-resource-area_th_fc-cell-content-z-index, 1); }
			.fc-resource-area th .fc-col-resizer { bottom: var(--fc-resource-area_th_fc-col-resizer-bottom, 0); }
			.fc-resource-area th .fc-col-resizer { position: var(--fc-resource-area_th_fc-col-resizer-position, absolute); }
			.fc-resource-area th .fc-col-resizer { top: var(--fc-resource-area_th_fc-col-resizer-top, 0); }
			.fc-resource-area th .fc-col-resizer { width: var(--fc-resource-area_th_fc-col-resizer-width, 5px); }
			.fc-resource-area th .fc-col-resizer { z-index: var(--fc-resource-area_th_fc-col-resizer-z-index, 2); }
			.fc-resource-area th > div { position: var(--fc-resource-area_th__LACE_BRACE__div-position, relative); }
			.fc-resource-area { width: var(--fc-resource-area-width, 30%); }
			.fc-rtl .fc-resource-area th .fc-col-resizer { left: var(--fc-rtl_fc-resource-area_th_fc-col-resizer-left, -3px); }
			.fc-rtl .fc-resource-area tr > * { text-align: var(--fc-rtl_fc-resource-area_tr__LACE_BRACE___ASTERISK_-text-align, right); }
			.fc-time-area .fc-rows .fc-bgevent-container { z-index: var(--fc-time-area_fc-rows_fc-bgevent-container-z-index, 1); }
			.fc-time-area .fc-rows .fc-highlight-container { z-index: var(--fc-time-area_fc-rows_fc-highlight-container-z-index, 1); }
			.fc-time-area .fc-rows td > div { position: var(--fc-time-area_fc-rows_td__LACE_BRACE__div-position, relative); }
			.fc-time-area .fc-rows { position: var(--fc-time-area_fc-rows-position, relative); }
			.fc-time-area .fc-rows { z-index: var(--fc-time-area_fc-rows-z-index, 3); }
			.fc-timeline .fc-body > tr > .fc-divider { border-top: var(--fc-timeline_fc-body__LACE_BRACE__tr__LACE_BRACE__fc-divider-border-top, 0); }
			.fc-timeline .fc-col-resizer { cursor: var(--fc-timeline_fc-col-resizer-cursor, col-resize); }
			.fc-timeline .fc-divider { border-style: var(--fc-timeline_fc-divider-border-style, double); }
			.fc-timeline .fc-divider { width: var(--fc-timeline_fc-divider-width, 3px); }
			.fc-timeline .fc-head > tr > .fc-divider { border-bottom: var(--fc-timeline_fc-head__LACE_BRACE__tr__LACE_BRACE__fc-divider-border-bottom, 0); }
        </style>
        `;
    }

    ready() {
        super.ready();
    }

    _createInitOptions(initialOptions) {
        const options = super._createInitOptions(initialOptions);

        if (initialOptions == null) {
            options.resources = [];
        }
        options.plugins.push(resourceTimeGridPlugin, resourceTimelinePlugin);
        return options;
    }

    addResources(array, scrollToLast) {
        let calendar = this.getCalendar();
        calendar.batchRendering(function () {
            for (let i = 0; i < array.length; i++) {
                calendar.addResource(array[i], scrollToLast);
            }
        });
    }

    removeResources(array) {
        let calendar = this.getCalendar();
        calendar.batchRendering(function () {
            for (let i = 0; i < array.length; i++) {
                const resource = calendar.getResourceById(array[i].id);
                if (resource != null) {
                    resource.remove();
                }
            }
        });
    }

    removeAllResources() {
        let calendar = this.getCalendar();
        calendar.batchRendering(function () {
            calendar.getResources().forEach(r => r.remove());
        });
    }

    setResourceRenderCallback(s) {
        var calendar = this.getCalendar();
        calendar.setOption('resourceRender', new Function("return " + s)());
    }
}

customElements.define(FullCalendarScheduler.is, FullCalendarScheduler);