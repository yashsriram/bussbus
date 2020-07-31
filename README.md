# bussbus

## description
- A simple tool for quickly glancing MetroTransit bus departures in Minneapolis/St. Paul.
- Stops can be added/removed.

## roadmap
- [x] Scroll view in stop departures activity.
- [x] Display stop name before requesting departuresList. Useful when departuresList comes with a lag.
- [x] Adding a stop.
    - [x] Where can stop ids be found? - Link provided.
    - [x] StopId validation: len == 5 & uniqueness.
    - [x] StopName validation: 0 < name & uniqueness.
    - [x] db add
    - [x] finish() on add.
- [x] Read stops from db.
- [x] Updated list on add/remove finish() return.
- [x] Removing a stop.
    - [x] Recycler view.
    - [x] db remove
    - [x] finish() on remove.
- [ ] Simplify Holder object.
- [ ] Inline shared pref commit()
- [ ] Error check shared pref commit()
- [ ] Improve Toast message
- [ ] Improve validation comments
- [ ] Simplify last sync x min ago periodic update.
- [ ] Improve styling.
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
- If a stop's departures can not be fetched, its id might be wrong. Try removing and adding it again.

## demonstration