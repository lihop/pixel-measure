/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import Point from './Point';
import Overlay from './Overlay';
import Ruler from './src/Ruler';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerComponent('Ruler', () => Ruler);
AppRegistry.registerComponent('Point', () => Point);
AppRegistry.registerComponent('Overlay', () => Overlay);
