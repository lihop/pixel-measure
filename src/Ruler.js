import React, {useEffect, useState, useRef} from 'react';
import ViewShot, {captureScreen} from 'react-native-view-shot';
import {
  Animated,
  StyleSheet,
  Dimensions,
  ScrollView,
  Image,
  Text,
  View,
  PanResponder,
  TouchableOpacity,
  TouchableHighlight,
  PixelRatio,
  StatusBar,
} from 'react-native';
import {css} from '@emotion/native';
import {range} from 'lodash';
import Icon from 'react-native-vector-icons/FontAwesome';
import MIcon from 'react-native-vector-icons/MaterialIcons';
import Draggable from 'react-native-draggable';
import Svg, {Line, Rect, Mask, Defs} from 'react-native-svg';
import OrientationLocker from 'react-native-orientation-locker';
import {
  FlingGestureHandler,
  Directions,
  State,
  RotationGestureHandler,
  PanGestureHandler,
} from 'react-native-gesture-handler';
import RulerView from './native/RulerView';
import Ruler from './native/RulerModule';
import {useStores} from './store/allStores';
import useInterval from 'react-use/lib/useInterval';

const USE_NATIVE_DRIVER = false;

const Orientation = Object.freeze({
  HORIZONTAL: Symbol('horizontal'),
  VERTICAL: Symbol('vertical'),
});

const RULER_WIDTH = 100;
const ONE_PIXEL = 1 / PixelRatio.get();

function Button({iconName, orientation, onPress, onLongPress, onPressOut}) {
  const flip = iconName === 'flip';

  return (
    <TouchableOpacity
      onStartShouldSetResponderCapture={e => {
        return true;
      }}
      onPress={onPress}
      onLongPress={onLongPress}
      onPressOut={onPressOut}
      style={css`
        margin: 2px;
        background-color: black;
        width: 30px;
        height: 30px;
        border-radius: 30px;
        justify-content: center;
        align-items: center;
        z-index: 3;
      `}>
      {!flip && (
        <Icon
          name={iconName}
          style={css`
            color: #7df22f;
            font-size: 20px;
          `}
        />
      )}
      {flip && (
        <MIcon
          name="flip"
          style={css`
            color: #7df22f;
            font-size: 20px;
            ${orientation == 'LANDSCAPE' && 'transform: rotate(90deg);'}
          `}
        />
      )}
    </TouchableOpacity>
  );
}

function Space({dp, orientation}) {
  return (
    <View
      style={css`
        ${orientation === 'PORTRAIT'
          ? 'height: '
          : 'width:'} ${dp.toString()}px;
      `}
    />
  );
}

export default ({
  orientation,
  screenWidth,
  screenHeight,
  screenOrientation,
}) => {
  const [width, setWidth] = useState(75);
  const [height, setHeight] = useState(75);
  const [locked, setLocked] = useState(false);
  const [lastRotate, setLastRotate] = useState(0);
  const [rotations, setRotatations] = useState(0);
  const [flipped, setFlipped] = useState(false);
  const [cursor, setCursor] = useState({isMoving: false, moveDirection: 'UP'});
  const rotate = new Animated.Value(0);

  // TODO: Need to have Activity in foreground for interval to work.
  //useInterval(
  //  () => {
  //    if (locked) {
  //      cursor.moveDirection === 'UP'
  //        ? Ruler.oneCursorPixelUp()
  //        : Ruler.oneCursorPixelDown();
  //    } else {
  //      cursor.moveDirection === 'UP'
  //        ? Ruler.onePixelUp()
  //        : Ruler.onePixelDown();
  //    }
  //  },
  //  cursor.isMoving ? 1 : null,
  //);

  useEffect(() => {
    Ruler.scrollTo(
      orientation === 'PORTRAIT' ? screenHeight / 2 : screenWidth / 2,
    );
  }, []);

  useEffect(() => {
    const screenHeightPx = screenHeight / PixelRatio.get();
    const screenWidthPx = screenWidth / PixelRatio.get();

    switch (screenOrientation) {
      // SCREEN PORTRAIT
      case 0:
      case 2:
        if (orientation === 'PORTRAIT') {
          setHeight(screenHeightPx);
          setWidth(RULER_WIDTH);
        } else {
          setHeight(RULER_WIDTH);
          setWidth(screenWidthPx);
        }
        break;

      // SCREEN LANDSCAPE
      case 1:
      case 3:
        if (orientation === 'PORTRAIT') {
          setHeight(screenHeightPx);
          setWidth(RULER_WIDTH);
        } else {
          setHeight(RULER_WIDTH);
          setWidth(screenWidthPx);
        }
    }
  });

  return (
    <RotationGestureHandler
      onGestureEvent={Animated.event([{nativeEvent: {rotation: rotate}}], {
        useNativeDriver: USE_NATIVE_DRIVER,
      })}
      onHandlerStateChange={event => {
        if (event.nativeEvent.oldState === State.ACTIVE) {
          Ruler.launch(
            orientation == 'PORTRAIT' ? 'LANDSCAPE' : 'PORTRAIT',
            false,
          );
        }
      }}>
      <Animated.View
        onStartShouldSetResponder={() => !locked}
        style={{
          ...css`
            width: ${width.toString()}px;
            height: ${height.toString()}px;
            background-color: rgba(255, 255, 255, 0.3);
            display: flex;
            flex-direction: ${orientation === 'PORTRAIT' ? 'column' : 'row'};
            overflow: visible;
            position: relative;
          `,
        }}>
        <RulerView
          orientation={orientation}
          lockRuler={locked}
          //onFlipped={({nativeEvent}) => {
          //  setFlipped(nativeEvent.flipped);
          //}}
          onOrientationChanged={({nativeEvent}) => {
            Ruler.restart(orientation);
          }}
          onStartShouldSetResponder={() => false}
          style={css`
            width: ${width.toString()}px;
            height: ${height.toString()}px;
          `}
        />
        <View
          style={css`
            position: absolute;
            width: ${width.toString()}px;
            height: ${height.toString()}px;
            align-items: ${flipped ? 'flex-start' : 'flex-end'};
            justify-content: center;
            flex-direction: ${orientation === 'PORTRAIT' ? 'column' : 'row'};
          `}>
          <Button iconName={'times'} onPress={() => Ruler.destroy()} />
          <Space dp={12} orientation={orientation} />
          <Button
            iconName={locked ? 'lock' : 'unlock'}
            onPress={() => setLocked(!locked)}
          />
          <Space dp={12} orientation={orientation} />
          <Button
            iconName={
              orientation === 'PORTRAIT' ? 'rotate-left' : 'rotate-right'
            }
            onPress={() =>
              Ruler.launch(
                orientation == 'PORTRAIT' ? 'LANDSCAPE' : 'PORTRAIT',
                false,
              )
            }
          />
          <Space dp={12} orientation={orientation} />
          <Button
            orientation={orientation}
            iconName="flip"
            onPress={() => {
              Ruler.flip();
              setFlipped(!flipped);
            }}
          />
          <Space dp={12} orientation={orientation} />
          <Button
            onPress={() => {
              !locked ? Ruler.onePixelUp() : Ruler.oneCursorPixelUp();
            }}
            onLongPress={() => setCursor({isMoving: true, moveDirection: 'UP'})}
            onPressOut={() => setCursor({isMoving: false})}
            iconName={orientation === 'PORTRAIT' ? 'caret-up' : 'caret-left'}
          />
          <Space dp={12} orientation={orientation} />
          <Button
            onPress={() => {
              !locked ? Ruler.onePixelDown() : Ruler.oneCursorPixelDown();
            }}
            onLongPress={() =>
              setCursor({isMoving: true, moveDirection: 'DOWN'})
            }
            onPressOut={() => setCursor({isMoving: false})}
            iconName={orientation === 'PORTRAIT' ? 'caret-down' : 'caret-right'}
          />
        </View>
      </Animated.View>
    </RotationGestureHandler>
  );
};
