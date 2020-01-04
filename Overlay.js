import React, {useEffect, useState} from 'react';
import {StyleSheet, Button, Image, View} from 'react-native';
import {useObserver} from 'mobx-react';
import {useStores} from './colorStore';
import {css} from '@emotion/native';
import Screenshot from './src/native/ScreenshotModule';

export default function Overlay() {
  const {color} = useStores();
  const [screenshot, setScreenshot] = useState(null);

  useEffect(() => {}, [color.color]);

  return useObserver(() => (
    <View
      style={{
        justifyContent: 'center',
        backgroundColor: color.color,
        width: 200,
        height: 200,
      }}>
      <Button
        title="set color"
        onPress={async () => {
          const randomColor = Math.floor(Math.random() * 16777215).toString(16);
          color.setColor('#' + randomColor);
          Screenshot.show('Awesome', Screenshot.SHORT);
          const data = await Screenshot.captureScreen();
          console.log(data);
          setScreenshot(data);
        }}></Button>
      <Image
        style={css`
          width: 50px;
          height: 50px;
        `}
        source={{uri: `data:image/bmp;base64,${screenshot}`}}
      />
    </View>
  ));
}
