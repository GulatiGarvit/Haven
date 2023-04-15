package in.safety.safety;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EmergencyContactsRecycler extends RecyclerView.Adapter<EmergencyContactsRecycler.ProgrammingViewHolder> {
    String[] phoneNumbers;
    public interface OnDeleteClickListener{
        void delete(int position);
    }
    private OnDeleteClickListener onDeleteClickListener;
    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener)
    {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public EmergencyContactsRecycler(String[] phoneNumbers)
    {
        this.phoneNumbers = phoneNumbers;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.emergency_contact_recycler,parent,false);
        return new ProgrammingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        holder.contactNumber.setText(phoneNumbers[position]);
    }

    @Override
    public int getItemCount() {
        return phoneNumbers.length;
    }

    public class ProgrammingViewHolder extends RecyclerView.ViewHolder
    {
        TextView contactNumber;
        ImageButton delete;
        public ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNumber = itemView.findViewById(R.id.contact_number);
            delete = itemView.findViewById(R.id.btn_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onDeleteClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION)
                        onDeleteClickListener.delete(getAdapterPosition());
                }
            });
        }
    }
}
