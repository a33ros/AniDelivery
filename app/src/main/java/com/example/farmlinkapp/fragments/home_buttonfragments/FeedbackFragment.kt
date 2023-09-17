package com.example.farmlinkapp.fragments.home_buttonfragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.Feedback
import com.example.farmlinkapp.databinding.FragmentFeedbackBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class FeedbackFragment : Fragment() {
    private lateinit var binding: FragmentFeedbackBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedbackBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedbackFragment_pop)
        }

        binding.sendFeedback.setOnClickListener {
            binding.sendFeedback.startAnimation()
            val firestore = Firebase.firestore
            val user = FirebaseAuth.getInstance()
            val userId = user.uid
            val rating = binding.ratingStars.rating
            val ratingDescription = binding.descriptionFeedback.text.toString().trim()
            val feedback = userId?.let { it1 ->
                Feedback(
                    it1,
                    rating,
                    ratingDescription
                )
            }

            if (rating == null) {
                Toast.makeText(requireContext(), "Rate the app first :)" , Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    firestore.collection("userFeedback").add(feedback!!).addOnSuccessListener {
                        binding.sendFeedback.revertAnimation()
                        findNavController().navigate(R.id.action_feedbackFragment_pop)
                        Toast.makeText(requireContext(), "Feedback has been sent to the developers!" , Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Log.e("Error", it.message.toString())
                    }
                }
            }
        }

    }
}