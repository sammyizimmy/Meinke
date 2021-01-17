# x-drv
# safely remove drive
if [ "$#" = 0 ]
then
   echo "usage: eject.sh mnt_path"
else
   dev_pth=$1
   if [ -d $dev_pth ]
   then
      tgt_dev=`findmnt -n -r -o SOURCE $dev_pth`
      umount $tgt_dev
      udisksctl power-off -b $tgt_dev
      echo "DONE"
   else
      echo "$dev_pth - not mounted"
   fi
fi
