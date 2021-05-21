package com.bestsearch.bestsearchservice.order.model.enums;

public enum
Status {
  SEARCHING, //order - just placed by user.
  INITIAL, //internal (assignment) - matched assignment but not assigned.
  PENDING, //assignment - assigned assignment and pending action.
  REJECTED, //assignment - rejected assignment by assignee. (cannot fulfill)
  ACCEPTED, //order and assignment - accepted order and assignment.
  CANCELLED, //order (,assignment) - cancelled by user while processing
  COMPLETED, //order and assignment - completed order, action triggered by order creator
  CANCELLED_BY_SYSTEM, //internal - cancelled by system, someone pick the order
  NO_RESPONSE //assignment - time passed since action was not triggered.
}
