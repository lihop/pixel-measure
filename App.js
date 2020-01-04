/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {useEffect} from 'react';
import {
  Alert,
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
  TouchableOpacity,
  Image,
} from 'react-native';

import {
  Header,
  LearnMoreLinks,
  Colors,
  DebugInstructions,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';
import {css} from '@emotion/native';
import EntypoIcon from 'react-native-vector-icons/Entypo';
import Orientation from 'react-native-orientation-locker';
import Ruler from './src/native/RulerModule';
import {observer} from 'mobx-react';
import {useStores} from './src/store/allStores';
import changeNavigationBarColor from 'react-native-navigation-bar-color';
import RNDrawOverlay from 'react-native-draw-overlay';

const App = () => {
  const {image} = useStores();

  useEffect(() => {
    changeNavigationBarColor('#ffffff');
  }, []);

  useEffect(() => {
    //console.log(image.images);
  }, [image.images]);

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView>
        <View
          style={css`
            height: 100%;
            background-color: #eed202;
            display: flex;
            padding: 16px;
          `}>
          <Text
            style={css`
              font-family: sans-serif;
              font-size: 60px;
              font-weight: 700;
            `}>
            Pixel Measure
          </Text>
          <TouchableOpacity
            onPress={async () => {
              try {
                await RNDrawOverlay.askForDispalayOverOtherAppsPermission();
                console.log('permissien gratnde!');

                Ruler.launch('PORTRAIT', true);
              } catch (e) {
                Alert.alert(
                  'Permission Required',
                  'In order to use this app permission is required to draw over other apps.',
                );
              }
            }}
            style={css`
              border: 5px solid black;
              width: 300px;
              height: 100px;
              margin-top: 50px;
            `}>
            <View
              style={css`
                flex: 1;
                display: flex;
                flex-direction: row;
                justify-content: space-around;
                align-items: center;
              `}>
              <EntypoIcon
                name="ruler"
                style={css`
                  margin: 20px;
                  font-size: 45px;
                `}
              />
              <Text
                style={css`
                  flex: 1;
                  font-size: 45px;
                  font-weight: 700;
                `}>
                Ruler
              </Text>
            </View>
          </TouchableOpacity>
          {false && image && image.images && image.images.length > 0 && (
            <Image
              width="200px"
              height="200px"
              style={css`
                width: 200px;
                height: 200px;
                background-color: green;
              `}
              source={{uri: image.images[0].uri}}
            />
          )}
        </View>
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({
  scrollView: {
    backgroundColor: Colors.lighter,
  },
  engine: {
    position: 'absolute',
    right: 0,
  },
  body: {
    backgroundColor: Colors.white,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
    color: Colors.black,
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
    color: Colors.dark,
  },
  highlight: {
    fontWeight: '700',
  },
  footer: {
    color: Colors.dark,
    fontSize: 12,
    fontWeight: '600',
    padding: 4,
    paddingRight: 12,
    textAlign: 'right',
  },
});

export default observer(App);
