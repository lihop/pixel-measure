import React from 'react';
import {AppRegistry, StyleSheet, Text, View} from 'react-native';

class HelloWorld extends React.Component {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.hello}>Hello, World</Text>
      </View>
    );
  }
}
var styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    backgroundColor: 'blue',
    opacity: 0.5,
    width: 50,
    height: 50,
  },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
});

AppRegistry.registerComponent('Ruler', () => HelloWorld);
