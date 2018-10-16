package com.parkme.Model;

public class Address {
    String address,column,row,floor;

    public Address() {
    }

    public Address(String address, String column, String row, String floor) {
        this.address = address;
        this.column = column;
        this.row = row;
        this.floor = floor;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }
}
