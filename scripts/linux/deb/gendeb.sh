#!/bin/sh -x
current_dir=$PWD
install_dir=install/deb/fluppy-1.0
install_deb=install/deb
final_name=fluppy-1.0

cd ../../../
mkdir -p $install_deb/$final_name/DEBIAN
mkdir -p $install_deb/$final_name/usr/bin
mkdir -p $install_deb/$final_name/usr/lib/fluppy
mkdir -p $install_deb/$final_name/usr/share/icons/hicolor/128x128/apps
mkdir -p $install_deb/$final_name/usr/share/applications
cp target/fluppy $install_deb/$final_name/usr/bin/
cp target/lib*.so $install_deb/$final_name/usr/lib/fluppy
cp src/main/resources/dosdog2.png install/deb/fluppy-1.0/usr/share/icons/hicolor/128x128/apps/fluppy.png
cp $current_dir/.desktop ${install_dir}/.desktop
cp $current_dir/control $install_deb/$final_name/DEBIAN/control
chmod +x ${install_dir}/usr/bin/fluppy
cd $install_deb
dpkg-deb --build fluppy-1.0