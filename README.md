# bussbus

## description
- A simple tool for quickly glancing MetroTransit bus departures in Minneapolis/St. Paul.
- Stops can be added/removed.

## roadmap
- [ ] Simplify last sync x min ago periodic update.
- [ ] Adding a stop.
    - [ ] Where can stop ids be found?
    - [ ] StopId validation: len.
    - [ ] StopName validation: name & uniqueness.
    - [ ] finish() on add.
- [ ] Removing a stop.
    - [ ] Recycler view.
    - [ ] finish() on remove.
- [ ] Updated list on add/remove finish() return.
- [x] Scroll view in stop departures activity.
- [x] Display stop name before requesting departuresList. Useful when departuresList comes with a lag.
- [ ] Decrease num of id references to xml elements.

## code
- This is a minimal android project.
- Uses MetroTransit public API <https://svc.metrotransit.org/nextrip>.

## documentation
- The documentation for the code is itself.

## usage
- Install the app on an android device.
- Add some stops (using respective stop ids) and give them (unique) names.
- The departures from those stops shall appear.
- The departures are not automatically updated. A sync button on the bottom shows how long ago was the last sync. Press that button to sync again.
- Similarly remove stops when not needed.
- No login required.

## demonstration