package com.healthshop.dto;

public class OrderStatusUpdateRequest {
  private Boolean confirmed;
  private Boolean shipped;

  public Boolean getConfirmed() { return confirmed; }
  public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }
  public Boolean getShipped() { return shipped; }
  public void setShipped(Boolean shipped) { this.shipped = shipped; }
}