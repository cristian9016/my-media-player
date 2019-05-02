package com.cristiandev.mymusicplayer.keyboard

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cristiandev.mymusicplayer.R
import com.cristiandev.mymusicplayer.databinding.FragmentKeyboardBinding
import kotlinx.android.synthetic.main.fragment_keyboard.*
import rx.subjects.PublishSubject

class KeyboardFragment : Fragment() {
    lateinit var binding: FragmentKeyboardBinding
    var string = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_keyboard, null, false)

        binding.btnA.setOnClickListener {
            string += "a"
            publishText.onNext(string)
        }
        binding.btnB.setOnClickListener {
            string += "b"
            publishText.onNext(string)
        }
        binding.btnC.setOnClickListener {
            string += "c"
            publishText.onNext(string)
        }
        binding.btnD.setOnClickListener {
            string += "d"
            publishText.onNext(string)
        }
        binding.btnE.setOnClickListener {
            string += "e"
            publishText.onNext(string)
        }
        binding.btnF.setOnClickListener {
            string += "f"
            publishText.onNext(string)
        }
        binding.btnG.setOnClickListener {
            string += "g"
            publishText.onNext(string)
        }
        binding.btnH.setOnClickListener {
            string += "h"
            publishText.onNext(string)
        }
        binding.btnI.setOnClickListener {
            string += "i"
            publishText.onNext(string)
        }
        binding.btnJ.setOnClickListener {
            string += "j"
            publishText.onNext(string)
        }
        binding.btnK.setOnClickListener {
            string += "k"
            publishText.onNext(string)
        }
        binding.btnL.setOnClickListener {
            string += "l"
            publishText.onNext(string)
        }
        binding.btnM.setOnClickListener {
            string += "m"
            publishText.onNext(string)
        }
        binding.btnN.setOnClickListener {
            string += "n"
            publishText.onNext(string)
        }
        binding.btnNh.setOnClickListener {
            string += "ñ"
            publishText.onNext(string)
        }
        binding.btnO.setOnClickListener {
            string += "o"
            publishText.onNext(string)
        }
        binding.btnP.setOnClickListener {
            string += "p"
            publishText.onNext(string)
        }
        binding.btnQ.setOnClickListener {
            string += "q"
            publishText.onNext(string)
        }
        binding.btnR.setOnClickListener {
            string += "r"
            publishText.onNext(string)
        }
        binding.btnS.setOnClickListener {
            string += "s"
            publishText.onNext(string)
        }
        binding.btnT.setOnClickListener {
            string += "t"
            publishText.onNext(string)
        }
        binding.btnU.setOnClickListener {
            string += "u"
            publishText.onNext(string)
        }
        binding.btnV.setOnClickListener {
            string += "v"
            publishText.onNext(string)
        }
        binding.btnW.setOnClickListener {
            string += "w"
            publishText.onNext(string)
        }
        binding.btnX.setOnClickListener {
            string += "x"
            publishText.onNext(string)
        }
        binding.btnY.setOnClickListener {
            string += "y"
            publishText.onNext(string)
        }
        binding.btnZ.setOnClickListener {
            string += "z"
            publishText.onNext(string)
        }
        binding.btnDel.setOnClickListener {
            if(string.isNotEmpty()){
                string = string.substring(0..(string.length - 2))
                publishText.onNext(string)
            }
        }
        return binding.root
    }

    companion object {
        val publishText = PublishSubject.create<String>()
    }
}